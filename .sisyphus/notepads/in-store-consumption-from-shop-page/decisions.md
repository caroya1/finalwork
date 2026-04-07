# Decisions - In-Store Consumption Feature

## 2025-04-07 Architecture Decisions

### Status Lifecycle
```
CouponPurchase.status:
- paid → used (on order create)
- used → paid (on cancel/timeout return)
- paid → refunded (explicit refund)
```

### Order Schema Changes
Add fields:
- `discount_amount` INT - 优惠金额
- `coupon_purchase_id` BIGINT - 具体使用的购买记录ID

### API Contracts

#### Coupon Internal API
```
POST /internal/coupons/consume
Request: { userId, couponId, shopId, orderNo }
Response: { success, purchaseId, discountAmount }

POST /internal/coupons/return
Request: { purchaseId, orderNo, reason }
Response: { success }
```

#### Order API (Frontend)
```
POST /api/orders (existing, enhanced)
Request: { userId, shopId, amount, couponId? }
Response: { orderNo, payAmount, discountAmount? }

The server computes payAmount from amount - discountAmount
```

### Concurrency Strategy
- Use Redis distributed lock for coupon consume
- Idempotency key: orderNo + couponId
- Status check: WHERE status = 'paid' in UPDATE

### Auto Best Coupon Algorithm
1. Get user's paid coupons for this shop
2. Filter by: status='paid', coupon.shopId matches
3. Sort by discountAmount DESC
4. Return top coupon that doesn't exceed order amount


## 2025-04-07 F4: Scope Fidelity Check Report

### Verdict: **CONDITIONAL APPROVE**

---

### TODO Completion Matrix

| TODO | Status | Evidence |
|------|--------|----------|
| T1: Schema/Status Contract | ✅ COMPLETE | Order.java has `discountAmount`, `couponPurchaseId`; CouponPurchase.java has `usedAt` and status lifecycle documented |
| T2: Coupon Consume Endpoint | ✅ COMPLETE | CouponInternalController.POST `/internal/coupons/consume`; CouponService.consumeCoupon() with Redisson lock |
| T3: Coupon Return Endpoint | ✅ COMPLETE | CouponInternalController.POST `/internal/coupons/return`; CouponService.returnCoupon() idempotent |
| T4: Order Create Integration | ✅ COMPLETE | OrderService.createOrder() calls couponClient.consumeCoupon(), server-side payAmount computation |
| T5: Cancel/Timeout Return | ✅ COMPLETE | OrderService.cancelOrder() calls returnCoupon(); timeout task delegates to cancelOrder() |
| T6: Shop Page UI | ✅ COMPLETE | ShopDetail.vue has amount input, auto-best-coupon, opt-out, payable summary, submit |
| T7: Failure State Handling | ❌ INCOMPLETE | Marked incomplete in plan - needs failure UX polish |
| T8: Tests-After Coverage | ❌ INCOMPLETE | Marked incomplete in plan - backend tests missing coupon paths, frontend only has sample test |

---

### Guardrail Compliance

| Guardrail | Status | Notes |
|-----------|--------|-------|
| No third-party payment gateway | ✅ PASS | No payment gateway added |
| No merchant/admin frontend overhaul | ✅ PASS | Only frontend-user modified |
| No notification system | ✅ PASS | No websocket/notification expansion |
| Single coupon only (no stacking) | ✅ PASS | Single coupon per order |

---

### Scope Fidelity Assessment

**NO SCOPE CREEP DETECTED**

All implemented features match plan requirements 1:1:
1. Coupon consume/return internal APIs implemented exactly as specified
2. Server-side discount computation enforced (client payAmount ignored)
3. Cancel/timeout compensation integrated
4. Shop page anchored in-store consumption UI
5. Auto-best coupon selection with opt-out

**Schema Decision**:
- SQL file (`dianping_order.sql`) not updated with new columns
- Learnings.md documents deliberate decision: "Project uses MyBatis-Plus auto-DDL"
- This is acceptable per project conventions (auto-table-creation)

---

### Missing Items (From Plan)

1. **T7: Failure State Handling** - Incomplete
   - Race conflict error UI needs work
   - Invalid coupon state handling needs verification

2. **T8: Test Coverage** - Incomplete
   - Backend: No tests for coupon consume/return paths
   - Frontend: Only sample.test.js exists, no component tests

---

### Recommendations

1. **Complete T7**: Add explicit error handling for race conditions, invalid coupon states
2. **Complete T8**: Add backend tests for coupon lifecycle, frontend tests for ShopDetail consume UI
3. **Update SQL schema** (optional): For documentation purposes, add new columns to dianping_order.sql

---

### Conclusion

The implementation faithfully follows the plan with no scope creep. The core functionality (T1-T6) is complete and functional. T7 and T8 are marked incomplete in the plan itself, indicating intentional partial delivery.

**APPROVE with conditions: T7 and T8 should be completed in follow-up work.**
