# In-Store Consumption from Shop Page (Auto Best Coupon)

## TL;DR

> Build a full frontend+backend in-store consumption flow anchored on `ShopDetail.vue`: user enters **original amount**, system auto-selects **best coupon**, user can opt out, and order is created with server-side validated discount.
>
> Coupon is **consumed at order creation** and **auto-returned** on cancel/timeout.

**Deliverables**:
- Shop page in-store consumption UI and order creation entry
- Backend atomic coupon validate+consume and return APIs
- Order-service discount computation + coupon integration
- Timeout cancel flow extended to return coupon
- Tests-after (backend unit/integration + frontend test infra) + agent QA evidence

**Estimated Effort**: Large
**Parallel Execution**: YES — 4 implementation waves + final verification wave
**Critical Path**: Schema & contract -> coupon consume/return internals -> order integration -> shop page UI -> timeout return integration -> verification

---

## Context

### Original Request
Add an in-store consumption feature in current project. User enters amount, can choose whether to use coupon, and create order from shop page.

### Interview Summary
**Confirmed decisions**:
- Page must be in shop page context (`ShopDetail`) (entry/hosted flow)
- Scope: frontend + backend together
- Coupon strategy: auto-select best coupon
- Amount input: original amount
- Coupon consume timing: consume at order creation
- Coupon on cancel/timeout: auto-return
- Test strategy: tests-after + mandatory agent-executed QA

**Research findings**:
- `ShopDetail.vue` already has coupon purchase UX and user-coupon loading hooks
- `order-service` currently accepts `couponId` but lacks strict ownership/validity checks and robust server-side discount enforcement
- `coupon-service` has user coupon listing internals but no explicit consume endpoint semantics with `used` lifecycle
- Existing timeout scheduler exists in order-service (`OrderTimeoutTask`) and cancels expired pending orders, but does not return coupon yet
- Frontend test infra missing (no Vitest/Jest currently)

### Metis Review (gaps addressed)
- Clarified amount semantics (original amount input)
- Clarified consume timing and rollback policy
- Added explicit guardrails to avoid scope creep (no merchant UI overhaul, no payment gateway redesign)
- Added criteria to force server-side discount computation (never trust client payAmount)
- Added edge-case coverage for concurrent coupon usage and timeout return correctness

---

## Work Objectives

### Core Objective
Implement in-store consumption order creation from shop page with optional coupon usage that is validated and computed server-side, with safe coupon lifecycle (consume on create, return on cancel/timeout).

### Concrete Deliverables
- Frontend-user shop page in-store amount input and coupon decision UI
- New/updated order API client module in frontend-user
- Backend coupon internal APIs for consume/return and ownership validation
- Order creation pipeline enforcing server-side final payable amount
- Timeout cancellation path that returns consumed coupon
- Automated tests (tests-after) and full agent QA evidence artifacts

### Definition of Done
- [ ] User can create in-store order from shop page by entering original amount
- [ ] System auto-selects best coupon and supports user opt-out
- [ ] Server computes final payable amount; client cannot spoof pay amount
- [ ] Coupon becomes consumed on order create and is restored on cancel/timeout
- [ ] Existing and new tests pass with QA evidence captured under `.sisyphus/evidence/`

### Must Have
- Shop-page anchored in-store consumption UX
- Atomic coupon validation/consumption semantics
- Cancel/timeout compensation (coupon return)
- Clear status lifecycle consistency across order and coupon purchase records

### Must NOT Have (Guardrails)
- No third-party payment gateway integration redesign
- No new merchant/admin front-end overhaul
- No websocket/notification architecture expansion
- No multi-coupon stacking in this scope (single best coupon only)

---

## Verification Strategy (MANDATORY)

> ZERO HUMAN INTERVENTION — ALL verification is agent-executed.

### Test Decision
- **Infrastructure exists**:
  - Backend: YES (JUnit5/Mockito)
  - Frontend-user: NO (must add minimal test infra in-plan)
- **Automated tests**: Tests-after
- **Frameworks**:
  - Backend: `mvn test`
  - Frontend-user: add Vitest + Vue Test Utils (minimal setup)

### QA Policy
Each task includes:
- 1 happy-path scenario
- 1 failure/edge scenario
- concrete tool, exact steps/assertions, and evidence path

Evidence format:
- `.sisyphus/evidence/task-{N}-{slug}.log|json|png`

---

## Execution Strategy

### Parallel Execution Waves

Wave 1 (Foundation / contracts / scaffolding):
- T1: DB lifecycle/status and schema updates
- T2: Coupon-service consume/return internal contract
- T3: Order-service integration contract + Feign client scaffolding
- T4: Frontend-user order API module + route scaffolding
- T5: Frontend-user test infra bootstrap (Vitest)

Wave 2 (Core backend logic):
- T6: Coupon consume logic (atomic validation + consume)
- T7: Coupon return logic (cancel/timeout compensation)
- T8: Order create discount pipeline (server-side compute + consume call)
- T9: Order cancel/timeout compensation integration
- T10: Backend tests-after for coupon/order lifecycle

Wave 3 (Core frontend logic on shop page):
- T11: ShopDetail in-store amount input + auto-best-coupon preview
- T12: User opt-out control + payable summary UX
- T13: Create-order action wiring + state handling
- T14: Failure UX states (invalid coupon, insufficient balance, race)

Wave 4 (Integration polish + hardening):
- T15: End-to-end API alignment adjustments (DTOs/messages)
- T16: Concurrency/idempotency hardening verification
- T17: Frontend tests-after (component + API interaction)
- T18: Documentation updates in relevant AGENTS/project notes if needed

Wave FINAL (Parallel verification):
- F1 Plan compliance audit (oracle)
- F2 Code quality review (unspecified-high)
- F3 Real QA execution (unspecified-high + playwright for UI)
- F4 Scope fidelity check (deep)

Critical Path:
T1 -> T2 -> T6 -> T8 -> T9 -> T11/T13 -> T15 -> F1-F4

### Dependency Matrix

- T1: Blocked By none | Blocks T6, T7, T8
- T2: Blocked By none | Blocks T6, T7, T8
- T3: Blocked By none | Blocks T8, T9
- T4: Blocked By none | Blocks T11, T12, T13
- T5: Blocked By none | Blocks T17
- T6: Blocked By T1,T2 | Blocks T8,T16
- T7: Blocked By T1,T2 | Blocks T9,T16
- T8: Blocked By T1,T2,T3,T6 | Blocks T13,T15,T10
- T9: Blocked By T3,T7,T8 | Blocks T15,T10
- T10: Blocked By T8,T9 | Blocks T16
- T11: Blocked By T4,T8 | Blocks T13,T14,T17
- T12: Blocked By T4,T8 | Blocks T13,T17
- T13: Blocked By T4,T8,T11,T12 | Blocks T15,T17
- T14: Blocked By T11,T13 | Blocks T15
- T15: Blocked By T8,T9,T13,T14 | Blocks FINAL
- T16: Blocked By T6,T7,T10 | Blocks FINAL
- T17: Blocked By T5,T11,T12,T13 | Blocks FINAL
- T18: Blocked By T15 | Blocks FINAL

### Agent Dispatch Summary

- Wave 1:
  - T1 `unspecified-high`
  - T2 `deep`
  - T3 `quick`
  - T4 `quick`
  - T5 `quick`
- Wave 2:
  - T6 `deep`
  - T7 `deep`
  - T8 `unspecified-high`
  - T9 `unspecified-high`
  - T10 `quick`
- Wave 3:
  - T11 `visual-engineering`
  - T12 `visual-engineering`
  - T13 `visual-engineering`
  - T14 `quick`
- Wave 4:
  - T15 `unspecified-high`
  - T16 `deep`
  - T17 `quick`
  - T18 `writing`
- Final:
  - F1 `oracle`, F2 `unspecified-high`, F3 `unspecified-high`, F4 `deep`

---

## TODOs

- [x] 1. Define status/schema contract for in-store consumption + coupon lifecycle

  **What to do**:
  - Add/confirm coupon purchase status lifecycle includes consumed/returned semantics (`paid -> used -> paid(refund/return)` where applicable)
  - Add/confirm order fields needed for explicit discount accounting (`amount`, `discount_amount`, `pay_amount`, `coupon_id`, optional `order_type`)
  - Ensure backward compatibility with existing order records and coupon purchases

  **Must NOT do**:
  - Do not introduce unrelated payment gateway schema changes
  - Do not add multi-coupon schema in this scope

  **Recommended Agent Profile**:
  - Category: `unspecified-high` (cross-service data contract)
  - Skills: []

  **Parallelization**:
  - Can Run In Parallel: YES (Wave 1)
  - Blocks: T6, T7, T8
  - Blocked By: None

  **References**:
  - `services/order-service/src/main/java/com/dianping/order/entity/Order.java`
  - `services/coupon-service/src/main/java/com/dianping/coupon/entity/CouponPurchase.java`
  - `sql/dianping_order.sql`

  **Acceptance Criteria**:
  - [ ] Schema/field contract documented and implemented with migration path

  **QA Scenarios**:
  ```
  Scenario: status transition legality
    Tool: Bash (curl + SQL client)
    Preconditions: test coupon purchase exists with status=paid
    Steps:
      1. Trigger consume API for that purchase
      2. Query purchase status from DB/API
      3. Trigger return path via cancel/timeout
      4. Re-query status
    Expected Result: paid -> used -> paid transition observed exactly once
    Evidence: .sisyphus/evidence/task-1-status-transition.log

  Scenario: backward compatibility
    Tool: Bash
    Preconditions: legacy records in dp_order and dp_coupon_purchase
    Steps:
      1. Run service startup + migrations
      2. Query legacy records
    Expected Result: no migration failure; legacy rows readable
    Evidence: .sisyphus/evidence/task-1-backward-compat.log
  ```

- [x] 2. Implement coupon internal consume endpoint (atomic validate + consume)

  **What to do**:
  - Add internal endpoint in coupon-service to validate ownership/shop/applicability and mark coupon used atomically
  - Include idempotency/race handling so duplicate consume requests cannot double-consume

  **Must NOT do**:
  - Do not expose consume endpoint publicly to external clients

  **Recommended Agent Profile**:
  - Category: `deep`
  - Skills: []

  **Parallelization**:
  - Can Run In Parallel: YES (Wave 2)
  - Blocks: T8, T16
  - Blocked By: T1, T2

  **References**:
  - `services/coupon-service/src/main/java/com/dianping/coupon/controller/CouponInternalController.java`
  - `services/coupon-service/src/main/java/com/dianping/coupon/service/CouponService.java`
  - `services/coupon-service/src/main/java/com/dianping/coupon/mapper/CouponPurchaseMapper.java`

  **Acceptance Criteria**:
  - [ ] Invalid ownership/shop coupon consume requests are rejected deterministically
  - [ ] Repeated consume request for same order context is idempotent

  **QA Scenarios**:
  ```
  Scenario: consume success
    Tool: Bash (curl)
    Preconditions: user owns paid coupon for target shop
    Steps:
      1. POST internal consume with userId/shopId/coupon/purchase context
      2. GET user coupons/internal view
    Expected Result: consume returns success, status becomes used
    Evidence: .sisyphus/evidence/task-2-consume-success.json

  Scenario: concurrent consume race
    Tool: Bash (parallel curl)
    Preconditions: one eligible coupon
    Steps:
      1. Fire two consume requests concurrently
      2. Collect both responses
    Expected Result: only one success, second deterministic failure/idempotent response
    Evidence: .sisyphus/evidence/task-2-race.log
  ```

- [x] 3. Implement coupon return endpoint for cancel/timeout compensation

  **What to do**:
  - Add internal return endpoint in coupon-service to restore coupon from used->paid for eligible cancel/timeout cases
  - Make return idempotent and safe for repeated scheduler/manual invocation

  **Must NOT do**:
  - Do not return coupons for completed/verified non-reversible flows

  **Recommended Agent Profile**:
  - Category: `deep`
  - Skills: []

  **Parallelization**:
  - Can Run In Parallel: YES (Wave 2)
  - Blocks: T9, T16
  - Blocked By: T1, T2

  **References**:
  - `services/coupon-service/src/main/java/com/dianping/coupon/service/CouponService.java`
  - `services/coupon-service/src/main/java/com/dianping/coupon/entity/CouponPurchase.java`

  **Acceptance Criteria**:
  - [ ] cancel/timeout can trigger coupon return exactly once

  **QA Scenarios**:
  ```
  Scenario: return on cancel
    Tool: Bash (curl)
    Preconditions: coupon already consumed by order create
    Steps:
      1. Invoke order cancel
      2. Read coupon status
    Expected Result: coupon status restored to paid
    Evidence: .sisyphus/evidence/task-3-return-on-cancel.log

  Scenario: idempotent return
    Tool: Bash
    Preconditions: same order already returned once
    Steps:
      1. Call return endpoint twice
      2. Inspect status and response
    Expected Result: no double side effect; deterministic response
    Evidence: .sisyphus/evidence/task-3-return-idempotent.log
  ```

- [x] 4. Integrate order create flow with server-side discount computation + consume call

  **What to do**:
  - Extend order-service create flow: compute final pay amount on server from original amount + selected/auto coupon
  - Call coupon internal consume contract before persisting final order state
  - Persist explicit amount breakdown

  **Must NOT do**:
  - Never trust any client-provided final pay amount

  **Recommended Agent Profile**:
  - Category: `unspecified-high`
  - Skills: []

  **Parallelization**:
  - Can Run In Parallel: NO (depends on consume contract)
  - Blocks: T10, T13, T15
  - Blocked By: T1, T2, T3, T6

  **References**:
  - `services/order-service/src/main/java/com/dianping/order/service/OrderService.java`
  - `services/order-service/src/main/java/com/dianping/order/dto/CreateOrderRequest.java`
  - `services/order-service/src/main/java/com/dianping/order/controller/OrderController.java`

  **Acceptance Criteria**:
  - [ ] final payable amount computed server-side and persisted
  - [ ] invalid/foreign coupon rejected

  **QA Scenarios**:
  ```
  Scenario: server ignores spoofed client final amount
    Tool: Bash (curl)
    Preconditions: valid coupon available
    Steps:
      1. POST create order with original amount + intentionally fake low payAmount field
      2. GET created order
    Expected Result: persisted payAmount equals server-computed value, not spoofed input
    Evidence: .sisyphus/evidence/task-4-server-compute.json

  Scenario: invalid coupon ownership
    Tool: Bash
    Preconditions: coupon belongs to different user
    Steps:
      1. Attempt create order with foreign coupon
    Expected Result: request rejected with clear error
    Evidence: .sisyphus/evidence/task-4-invalid-coupon.log
  ```

- [x] 5. Integrate cancel + timeout compensation path to return coupon

  **What to do**:
  - On manual cancel path, call coupon return for consumed coupon orders
  - Extend existing timeout task to perform same compensation for expired pending orders

  **Must NOT do**:
  - Do not create a second competing timeout scheduler

  **Recommended Agent Profile**:
  - Category: `unspecified-high`
  - Skills: []

  **Parallelization**:
  - Can Run In Parallel: NO (depends on return contract)
  - Blocks: T10, T15
  - Blocked By: T3, T7, T8

  **References**:
  - `services/order-service/src/main/java/com/dianping/order/task/OrderTimeoutTask.java`
  - `services/order-service/src/main/java/com/dianping/order/service/OrderService.java`

  **Acceptance Criteria**:
  - [ ] cancel + timeout both restore coupon state for eligible orders

  **QA Scenarios**:
  ```
  Scenario: timeout compensation
    Tool: Bash (curl + logs)
    Preconditions: order in pending state beyond timeout threshold with consumed coupon
    Steps:
      1. Trigger/await timeout task
      2. Verify order status transitioned to cancelled
      3. Verify coupon returned to paid
    Expected Result: both order and coupon states consistent
    Evidence: .sisyphus/evidence/task-5-timeout-compensation.log

  Scenario: cancel compensation
    Tool: Bash
    Preconditions: pending order with consumed coupon
    Steps:
      1. Call cancel endpoint
      2. Verify coupon state
    Expected Result: coupon returned once; no duplicate effects on repeated cancel
    Evidence: .sisyphus/evidence/task-5-cancel-compensation.log
  ```

- [x] 6. Build shop-page in-store consumption UX with auto-best coupon + opt-out

  **What to do**:
  - In `ShopDetail.vue`, add in-store amount input (original amount)
  - Auto-calculate/select best coupon candidate for current shop and user-owned coupons
  - Provide explicit toggle to not use coupon
  - Show clear final payable preview before submit

  **Must NOT do**:
  - Do not move this flow out of shop page

  **Recommended Agent Profile**:
  - Category: `visual-engineering`
  - Skills: []

  **Parallelization**:
  - Can Run In Parallel: YES (Wave 3)
  - Blocks: T13, T14, T17
  - Blocked By: T4, T8

  **References**:
  - `frontend-user/src/views/ShopDetail.vue`
  - `frontend-user/src/api/shop.js`
  - `frontend-user/src/views/UserProfile.vue` (owned coupon source shape)

  **Acceptance Criteria**:
  - [ ] user can input original amount and see computed payable
  - [ ] user can accept auto-best or opt-out

  **QA Scenarios**:
  ```
  Scenario: auto-best coupon display
    Tool: Playwright
    Preconditions: user logged in; owns multiple coupons for this shop
    Steps:
      1. Open /shops/{id}
      2. Enter amount "300"
      3. Observe selected coupon and payable summary
    Expected Result: highest applicable discount coupon auto-selected; payable updated
    Evidence: .sisyphus/evidence/task-6-auto-best.png

  Scenario: opt-out coupon
    Tool: Playwright
    Preconditions: same page state
    Steps:
      1. Toggle "不使用优惠券"
      2. Observe payable summary
    Expected Result: payable equals original amount with no coupon deduction
    Evidence: .sisyphus/evidence/task-6-opt-out.png
  ```

- [ ] 7. Wire create-order submission from shop page and handle failure states

  **What to do**:
  - Implement submit flow from shop page using order API module
  - Handle backend rejection states (invalid coupon, race conflict, timeout, auth)
  - Ensure post-submit state transitions (success redirect/feedback) are deterministic

  **Must NOT do**:
  - Do not silently ignore backend errors

  **Recommended Agent Profile**:
  - Category: `visual-engineering`
  - Skills: []

  **Parallelization**:
  - Can Run In Parallel: NO (depends on T6/T8 + UI)
  - Blocks: T15, T17
  - Blocked By: T4, T8, T11, T12

  **References**:
  - `frontend-user/src/api/client.js`
  - `frontend-user/src/router/index.js`

  **Acceptance Criteria**:
  - [ ] successful create leads to clear success state
  - [ ] known failures are surfaced with actionable messages

  **QA Scenarios**:
  ```
  Scenario: create success
    Tool: Playwright
    Preconditions: valid amount + eligible coupon
    Steps:
      1. Submit order from shop page
      2. Observe success feedback and destination state
    Expected Result: order created; displayed payable matches backend value
    Evidence: .sisyphus/evidence/task-7-create-success.png

  Scenario: consume race failure
    Tool: Playwright + Bash (pre-trigger race)
    Preconditions: coupon consumed by parallel flow
    Steps:
      1. Attempt submit with now-unavailable coupon
    Expected Result: deterministic error shown; user can reattempt without stale state
    Evidence: .sisyphus/evidence/task-7-race-failure.png
  ```

- [ ] 8. Add tests-after coverage (backend + frontend) and ensure commandability

  **What to do**:
  - Backend: add/extend tests for create, consume, cancel, timeout compensation
  - Frontend-user: add minimal Vitest infra and targeted tests for amount/coupon decision UI
  - Ensure repeatable test commands documented and passing

  **Must NOT do**:
  - Do not overbuild full CI/CD redesign in this task

  **Recommended Agent Profile**:
  - Category: `quick`
  - Skills: []

  **Parallelization**:
  - Can Run In Parallel: PARTIAL (backend/frontend split)
  - Blocks: FINAL wave
  - Blocked By: T5, T8, T9, T11, T12, T13

  **References**:
  - `services/order-service/src/test/java/com/dianping/order/service/OrderServiceTest.java`
  - `services/coupon-service/src/test/java/com/dianping/coupon/stress/SeckillStressTest.java`
  - `frontend-user/package.json`

  **Acceptance Criteria**:
  - [ ] backend tests for new lifecycle paths pass
  - [ ] frontend tests execute in module command line

  **QA Scenarios**:
  ```
  Scenario: backend lifecycle test command
    Tool: Bash
    Preconditions: dependencies running/test env prepared
    Steps:
      1. Run mvn test in order-service and coupon-service for updated tests
    Expected Result: PASS with zero failures for new test classes
    Evidence: .sisyphus/evidence/task-8-backend-tests.log

  Scenario: frontend test run
    Tool: Bash
    Preconditions: frontend test deps installed
    Steps:
      1. Run frontend-user test command
    Expected Result: test runner executes and relevant tests pass
    Evidence: .sisyphus/evidence/task-8-frontend-tests.log
  ```

---

## Final Verification Wave (MANDATORY)

- [ ] F1. **Plan Compliance Audit** — `oracle`
  Validate each must-have and guardrail against actual implementation + evidence files.

- [ ] F2. **Code Quality Review** — `unspecified-high`
  Run static checks/tests and inspect anti-patterns.

- [ ] F3. **Real QA Execution** — `unspecified-high` (+ `playwright` for UI)
  Execute all task QA scenarios, capture evidence under `.sisyphus/evidence/final-qa/`.

- [ ] F4. **Scope Fidelity Check** — `deep`
  Ensure no scope creep and all planned contracts are implemented 1:1.

---

## Commit Strategy

- Commit 1: Schema/status + DTO contract updates
- Commit 2: coupon-service internal consume/return APIs
- Commit 3: order-service compute+consume integration + cancel/timeout return
- Commit 4: frontend-user order API + shop page UI/UX flow
- Commit 5: tests-after additions (backend + frontend)
- Commit 6: integration polish and documentation notes

---

## Success Criteria

### Verification Commands
```bash
# Backend module tests
cd services/order-service && mvn test
cd ../coupon-service && mvn test

# Frontend tests-after
cd ../../frontend-user && npm run test

# (Optional integration smoke)
# Create order from shop page flow via Playwright scenario runner
```

### Final Checklist
- [ ] Shop page in-store order flow works end-to-end
- [ ] Auto-best coupon selection and opt-out both work
- [ ] Server-side payable computation enforced
- [ ] Coupon consumed on create and returned on cancel/timeout
- [ ] Tests-after pass and QA evidence present
