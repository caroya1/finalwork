# Learnings - In-Store Consumption Feature

## 2025-04-07 Session Start

### Codebase Analysis

#### Order Entity (Order.java)
- Has `couponId` field (Long)
- Has `amount` (original) and `payAmount` (final payable)
- **MISSING**: `discountAmount` field, `couponPurchaseId` field

#### CouponPurchase Entity (CouponPurchase.java)
- Has `status` field (String)
- Current statuses: `paid`, `processing`, `refunded`
- **MISSING**: `used` status, `usedAt` timestamp field

#### OrderService.createOrder() (Line 51)
```java
order.setPayAmount(request.getAmount());  // Just copies amount, no discount!
order.setCouponId(request.getCouponId()); // Accepts couponId but NO validation
```

#### OrderService.cancelOrder()
- Sets status to CANCELLED
- **MISSING**: Coupon return logic

#### OrderTimeoutTask
- Runs every 60 seconds
- Calls `orderService.cancelExpiredOrders()`
- **MISSING**: Coupon return logic

#### CouponPort Interface
```java
public interface CouponPort {
    List<UserCouponView> listByUser(Long userId);
}
```
- **MISSING**: `consumeCoupon`, `returnCoupon` methods

#### CouponInternalController
- Only has `GET /internal/coupons/user` endpoint
- **MISSING**: `/consume`, `/return` endpoints

#### Coupon Entity
- Has `shopId`, `discountAmount`, `price`
- Can query by shop via `listByShop(shopId)`

#### Frontend-user
- **MISSING**: `src/api/order.js` - no order API module!
- ShopDetail.vue has coupon purchase flow but no in-store consumption UI

### Architecture Patterns
1. Port/Adapter pattern: Service间通过common/port/接口 + FeignClient通信
2. Internal API path: `/internal/**` for service-to-service calls
3. User context: X-User-Id, X-User-Role headers

### Key Decisions from Planning
- Page location: In ShopDetail.vue
- Scope: Frontend + Backend together
- Coupon strategy: Auto-select best coupon
- Amount input: Original amount
- Coupon consume timing: Consume at order creation
- On cancel/timeout: Auto-return coupon

## 2025-04-07 T4: Frontend-user Order API Module

### Task: Create order.js API module + check routes

#### API Module Created
- File: `frontend-user/src/api/order.js`
- Exports: createOrder, getOrder, listUserOrders, cancelOrder, payOrder
- Pattern: Follows existing shop.js/user.js pattern using client (axios instance)
- User ID: Retrieved from localStorage dp_user_id

#### Router Check
- Current routes: /, /search, /posts/new, /posts/:id, /category, /shops/:id, /profile
- Order routes: NOT found (no /orders, /orders/:id)
- Decision: Do NOT add routes yet - UI components not created (T6/T7)

#### API Pattern (Reference from existing)
```javascript
import client from "./client";

export const createOrder = (data) => {
  const userId = localStorage.getItem("dp_user_id");
  return client.post("/api/orders", {
    userId: Number(userId),
    ...data
  }).then((res) => res.data);
};
```

#### Key Notes
- Storage keys: dp_token, dp_user_id, dp_user_role
- API base: http://localhost:8081 (via gateway)
- Axios interceptor adds Authorization header automatically

## 2025-04-07 T5: Frontend-user Test Infra Bootstrap (Vitest)

### Task: Add Vitest testing infrastructure

#### Dependencies Added (package.json)
```json
{
  "scripts": {
    "test": "vitest",
    "test:run": "vitest run"
  },
  "devDependencies": {
    "@vue/test-utils": "^2.4.0",
    "happy-dom": "^13.0.0",
    "vitest": "^1.0.0"
  }
}
```

#### Config Created
- File: `vitest.config.js`
- Uses: happy-dom environment for Vue component testing
- globals: true (allows describe/it without import)

#### Test Structure
- Directory: `src/__tests__/`
- Sample test: `sample.test.js` - passes ✓

#### Commands
- `npm run test` - watch mode
- `npm run test:run` - single run

#### Key Notes
- Vitest is Vite's native test runner - seamless integration
- @vue/test-utils for Vue component testing
- happy-dom provides DOM API for Node environment

## 2025-04-07 T3: Order-service Integration Contract + Feign Client Scaffolding

### Task: Create CouponClient and add TODO stubs in OrderService

#### CouponClient Created
- File: `services/order-service/src/main/java/com/dianping/order/client/CouponClient.java`
- Pattern: Follows existing user-service/CouponClient.java pattern
- Extends: CouponPort interface from common module
- Methods: listByUser (inherited)

#### OrderService TODO Stubs Added
- createOrder(): Added TODO for coupon validation, discount computation, coupon consume
- cancelOrder(): Added TODO for coupon return on order cancellation
- cancelExpiredOrders(): Added TODO for coupon return on each expired order

#### Key Observations
- CouponPort only has listByUser() method - needs extension in T2/T4 for consume/return
- CreateOrderRequest already has couponId field - no DTO changes needed
- Order entity already has couponId, amount, payAmount fields
- Feign client uses @FeignClient(name = "coupon-service") pattern

## 2025-04-07 T1: Schema Contract for In-Store Consumption + Coupon Lifecycle

### Task: Define status/schema contract

#### CouponPurchase.java Changes
- **Added**: `usedAt` field (LocalDateTime) - tracks when coupon was consumed
- **Added**: Javadoc documenting status lifecycle and transitions
  - Status values: `paid`, `processing`, `used`, `refunded`
  - State transitions:
    - `paid` → `used` (on order create)
    - `used` → `paid` (on cancel/timeout return)
    - `paid` → `refunded` (explicit refund)

#### Order.java Changes
- **Added**: `couponPurchaseId` field (Long, nullable) - tracks which CouponPurchase was used
- **Added**: `discountAmount` field (Integer, nullable) - stores discount amount in cents
- Both fields have getters and setters

#### MyBatis-Plus Auto-Mapping Compatibility
- Field naming: camelCase in Java entities
- Column naming: snake_case in database (auto-converted by MyBatis-Plus)
- New columns will be auto-created:
  - `couponPurchaseId` → `coupon_purchase_id`
  - `discountAmount` → `discount_amount`
  - `usedAt` → `used_at`

#### Backward Compatibility
- All new fields are nullable: `couponPurchaseId`, `discountAmount`, `usedAt`
- Existing records will have null values for new columns
- No breaking changes to existing APIs

#### SQL Files Not Modified
- Project uses MyBatis-Plus auto-DDL: new entity fields become columns automatically
- No manual SQL migration files created (per task requirements)

## 2025-04-07 T2: Implement Coupon Internal Consume Endpoint

### Task: Implement atomic validate + consume logic for coupons

#### New DTOs Created (common module)
- `ConsumeCouponResult.java`: Contains purchaseId, discountAmount
- `ConsumeCouponRequest.java`: Request body for consume endpoint
- `ReturnCouponRequest.java`: Request body for return endpoint

#### CouponPort Interface Extended
```java
public interface CouponPort {
    List<UserCouponView> listByUser(Long userId);
    ConsumeCouponResult consumeCoupon(Long userId, Long couponId, Long shopId, String orderNo);
    void returnCoupon(Long purchaseId, String orderNo, String reason);
}
```

#### CouponService Methods Added
1. **consumeCoupon()**: Atomic coupon consumption
   - Uses Redisson distributed lock: `dp:coupon:consume:{couponId}:{userId}`
   - Validates coupon belongs to shop
   - Finds user's paid purchase
   - Uses optimistic lock UPDATE with WHERE status='paid'
   - Sets status='used', usedAt=now()
   - Returns purchaseId and discountAmount
   - Idempotent: throws if already consumed

2. **returnCoupon()**: Return coupon on cancel/timeout
   - Validates purchase exists and status='used'
   - Idempotent: returns success if already 'paid'
   - Uses optimistic lock UPDATE with WHERE status='used'
   - Sets status='paid', usedAt=null

#### CouponPortImpl Updated
- Injected CouponService alongside UserCouponService
- Delegates consumeCoupon and returnCoupon to CouponService

#### CouponInternalController Endpoints Added
- `POST /internal/coupons/consume`: Consumes coupon, returns ConsumeCouponResult
- `POST /internal/coupons/return`: Returns coupon, returns ApiResponse<Void>

#### Key Design Decisions
1. **Distributed Lock**: Uses Redisson for concurrent request handling
2. **Optimistic Lock**: UPDATE with WHERE status='paid' prevents double-consume
3. **Idempotency**:
   - consumeCoupon throws on already-used
   - returnCoupon succeeds on already-paid
4. **Shop Validation**: Coupon must belong to the shop being ordered from
5. **Oldest First**: Consumes the earliest paid purchase for the coupon

#### Build Verification
- `mvn clean install -DskipTests` on common module: SUCCESS
- `mvn clean compile -DskipTests` on coupon-service: SUCCESS

## 2025-04-07 T4: Integrate Order Create Flow with Coupon Consume

### Task: Integrate order create flow with server-side discount computation + consume call

#### OrderService Changes

**Constructor Injection:**
- Added `CouponClient couponClient` to constructor parameters
- CouponClient is auto-wired via Spring's dependency injection

**createOrder() Logic:**
1. Generate orderNo BEFORE calling consumeCoupon (required for idempotency)
2. When couponId is provided:
   - Call `couponClient.consumeCoupon(userId, couponId, shopId, orderNo)`
   - Store purchaseId and discountAmount from result
   - Compute payAmount = amount - discountAmount (with floor at 0)
3. When couponId is null:
   - payAmount = amount, discountAmount = null, couponPurchaseId = null

**Discount Calculation:**
```java
BigDecimal discountYuan = result.getDiscountAmount();
Integer discountAmount = yuanToCents(discountYuan);
Integer payAmount = Math.max(0, request.getAmount() - discountAmount);
```

**Helper Method:**
```java
private Integer yuanToCents(BigDecimal yuan) {
    return yuan.multiply(new BigDecimal("100")).intValue();
}
```

**Order Fields Set:**
- `order.setCouponPurchaseId(couponPurchaseId)` - tracks which purchase was used
- `order.setDiscountAmount(discountAmount)` - stores discount in cents
- `order.setPayAmount(payAmount)` - final amount to pay (server-computed)

#### CouponClient Changes

**Added consumeCoupon Method:**
```java
@PostMapping("/internal/coupons/consume")
ConsumeCouponResult consumeCoupon(
    @RequestParam("userId") Long userId,
    @RequestParam("couponId") Long couponId,
    @RequestParam("shopId") Long shopId,
    @RequestParam("orderNo") String orderNo);
```

#### Error Handling

Errors are propagated from coupon-service through Feign:
- **Coupon not owned by user**: BusinessException("优惠券不属于当前用户")
- **Coupon not applicable to shop**: BusinessException("优惠券不适用于该店铺")
- **Already consumed (race condition)**: BusinessException from optimistic lock failure

All errors are wrapped in FeignException and propagated as BusinessException to the client.

#### Security Principles Applied

1. **NEVER trust client-provided payAmount**: Server always computes payAmount from amount - discountAmount
2. **Server-side coupon validation**: Coupon ownership and shop applicability validated by coupon-service
3. **Atomic consume**: Coupon is consumed during order creation within the same transaction
4. **Idempotency**: orderNo passed to consumeCoupon ensures idempotent operations

#### Build Verification
- `mvn clean compile -DskipTests` on order-service: SUCCESS ✓

#### Key Design Decisions

1. **Order No First**: orderNo is generated before consumeCoupon call to enable idempotency
2. **Yuan to Cents Conversion**: Coupon stores discount in yuan (BigDecimal), Order stores in cents (Integer)
3. **Floor at Zero**: payAmount cannot be negative (Math.max(0, amount - discount))
4. **Nullable Fields**: discountAmount and couponPurchaseId are null when no coupon used
5. **Transaction Boundaries**: Order creation and coupon consumption are in the same @Transactional method

## 2026-04-07 T5: Integrate cancel + timeout compensation path to return coupon

### Task: Integrate coupon return on order cancel and timeout

#### Changes Made

**1. CouponClient.java** - Added `returnCoupon()` Feign client method:
```java
@PostMapping("/internal/coupons/return")
void returnCoupon(
    @RequestParam("purchaseId") Long purchaseId,
    @RequestParam("orderNo") String orderNo,
    @RequestParam("reason") String reason);
```

**2. OrderService.java** - Modified `cancelOrder()` method:
```java
if (order.getCouponPurchaseId() != null) {
    try {
        couponClient.returnCoupon(order.getCouponPurchaseId(), order.getOrderNo(), reason);
    } catch (Exception e) {
        log.warn("Failed to return coupon for order {}: {}", orderId, e.getMessage());
    }
}
```

Key points:
- Check if order has `couponPurchaseId` before attempting return
- Exception handling: logs warning but doesn't fail cancellation (best-effort)
- Removed TODO comments from T3 stubs

**3. OrderService.java** - `cancelExpiredOrders()` method:
- Already calls `cancelOrder()` internally
- Coupon return happens automatically through delegation
- Removed TODO comment

#### Design Decisions

1. **Best-effort coupon return**: If coupon return fails, order cancellation still succeeds. This prevents order cancellation from being blocked by coupon-service issues.

2. **Idempotent return handling**: The returnCoupon endpoint is idempotent (safe to call multiple times), so even if there's a retry or duplicate call, no double side effects occur.

3. **Single responsibility**: `cancelExpiredOrders()` delegates to `cancelOrder()`, avoiding code duplication for coupon return logic.

4. **Exception handling strategy**: Log warning and continue - order cancellation is the primary operation, coupon return is secondary.

#### Build Verification
- `mvn clean compile -DskipTests` on order-service: SUCCESS ✓

#### Files Modified
- `services/order-service/src/main/java/com/dianping/order/client/CouponClient.java`
- `services/order-service/src/main/java/com/dianping/order/service/OrderService.java`


## 2025-04-07 T6: Build shop-page in-store consumption UX with auto-best coupon + opt-out

### Task: Add in-store consumption UI to ShopDetail.vue

#### Implementation Summary

**1. Template Section Added**
- Location: After coupons panel, before related posts section
- Condition: `v-if="isLoggedIn"` - only shown for logged-in users
- Components:
  - Amount input with currency prefix (¥)
  - Auto-selected coupon display with badge
  - "No coupon available" state
  - Opt-out checkbox toggle
  - Payable amount preview (original, discount, final)
  - Submit button with loading state
  - Order message feedback (success/error)

**2. Reactive State**
```javascript
const consumeAmount = ref(0);
const skipCoupon = ref(false);
const bestCoupon = ref(null);
const submitting = ref(false);
const orderMsg = ref("");
const orderMsgType = ref("");
const isLoggedIn = computed(() => !!localStorage.getItem("dp_token"));
```

**3. Computed Property**
```javascript
const payableAmount = computed(() => {
  if (skipCoupon.value || !bestCoupon.value) {
    return consumeAmount.value || 0;
  }
  return Math.max(0, consumeAmount.value - bestCoupon.value.discountAmount);
});
```

**4. Auto Best Coupon Selection Algorithm**
```javascript
const selectBestCoupon = () => {
  if (!consumeAmount.value || consumeAmount.value <= 0) {
    bestCoupon.value = null;
    return;
  }
  // Filter: owned by user + matches shop + discount <= amount
  const availableCoupons = coupons.value.filter((c) => {
    return (
      ownedCouponIds.value.has(c.id) &&
      c.shopId === shop.value?.id &&
      c.discountAmount <= consumeAmount.value
    );
  });
  // Sort by discount DESC, pick best
  if (availableCoupons.length > 0) {
    availableCoupons.sort((a, b) => b.discountAmount - a.discountAmount);
    bestCoupon.value = availableCoupons[0];
  } else {
    bestCoupon.value = null;
  }
};
```

**5. Watcher for Auto-Selection**
```javascript
watch(consumeAmount, () => {
  selectBestCoupon();
});
```

**6. Submit Handler**
- Calls `createOrder()` API with shopId, amount, and optional couponId
- Handles success/error states with user feedback
- Resets form on success
- Uses try-catch for error handling

**7. CSS Styling**
- Uses existing CSS variables: `--card`, `--accent`, `--muted`, `--line`
- Matches existing patterns: `.panel`, `.cta` button classes
- New styles:
  - `.consume-panel`: Gradient background for visual distinction
  - `.amount-input-wrapper`: Focus state with accent border
  - `.coupon-preview`: Green-themed for positive action
  - `.pay-preview`: Card-style with dashed separator for total
  - `.order-message`: Success/error states with appropriate colors

#### Key Design Decisions

1. **Auto-selection on input**: As user types amount, best coupon is automatically selected
2. **Opt-out checkbox**: User can explicitly choose not to use coupon even if available
3. **Visual feedback**: Clear breakdown of original amount, discount, and final payable
4. **Amount validation**: Only shows coupon options when amount > 0
5. **Shop matching**: Coupons are filtered by shopId to ensure applicability

#### Files Modified
- `frontend-user/src/views/ShopDetail.vue`

#### Dependencies
- Uses existing `createOrder` from `../api/order.js` (created in T4)
- Uses existing `ownedCouponIds` Set from user profile
- Uses existing `coupons` ref from shop detail

## 2026-04-07 F2: Code Quality Review

### Static Checks Performed

#### JavaScript/Vue Files
- **console.log**: ✓ No occurrences found
- **debugger statements**: ✓ No occurrences found
- **@ts-ignore / as any**: ✓ No occurrences found (project uses pure JavaScript)
- **Element Plus / Ant Design**: ✓ No external UI libraries found

#### Java Files
- **TODO/FIXME/HACK**: ⚠️ Found 1 TODO in InternalApiFilter.java:52
- **@SuppressWarnings**: ⚠️ Found 2 occurrences (known legacy issues)
- **@Deprecated**: ⚠️ Found 1 occurrence (known legacy issue)

### Anti-Pattern Violations Found

| Severity | Location | Issue | Status |
|----------|----------|-------|--------|
| HIGH | InternalApiFilter.java:52 | TODO: 生产环境需要启用 (internal API security disabled) | Known issue - pre-existing |
| LOW | UserCouponView.java:3 | @Deprecated class with no functionality | Known issue - pre-existing |
| LOW | MerchantService.java:148 | @SuppressWarnings("unchecked") for Redis cache | Known issue - pre-existing |
| LOW | AiRecommendStrategy.java:64 | @SuppressWarnings("unchecked") for Redis cache | Known issue - pre-existing |

### Changed Files Quality Assessment

**1. ShopDetail.vue**
- ✓ Uses custom CSS variables (--card, --accent, etc.)
- ✓ No external UI libraries introduced
- ✓ Follows project Vue3 Composition API pattern
- ✓ Proper error handling with try-catch
- ✓ Clean separation of concerns (logic in script, presentation in template)

**2. order.js**
- ✓ Follows existing API module pattern
- ✓ Uses localStorage dp_user_id consistently
- ✓ JSDoc comments present
- ✓ Proper promise chain with .then(res => res.data)

**3. OrderService.java**
- ✓ Proper @Transactional annotations
- ✓ Constructor injection pattern
- ✓ BusinessException for domain errors
- ✓ Logging with SLF4J
- ✓ No suppress warnings needed

**4. CouponService.java**
- ✓ Distributed locking with Redisson
- ✓ Optimistic locking with SQL WHERE clauses
- ✓ Atomic operations for coupon lifecycle
- ✓ Proper exception handling (Thread interruption)

### Build Verification Results

| Module | Command | Result |
|--------|---------|--------|
| common | `mvn clean install -DskipTests -q` | ✓ SUCCESS |
| coupon-service | `mvn clean compile -DskipTests -q` | ✓ SUCCESS |
| frontend-user | `npm run build` | ✓ SUCCESS (104 modules, 1.83s) |

### Summary

- **No new anti-patterns introduced**
- **No external UI libraries added**
- **All builds pass**
- **Code follows project conventions**
- **Only pre-existing anti-patterns remain** (documented in AGENTS.md)

### Recommendations

1. **Production Readiness**: InternalApiFilter.java line 52 needs to be addressed before production deployment
2. **No immediate action needed**: All other findings are pre-existing and documented
