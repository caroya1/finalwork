# Issues - In-Store Consumption Feature

## Known Issues

### 1. Order Service Missing Coupon Validation
**Location**: OrderService.java:49-51
**Issue**: Accepts couponId without checking ownership or validity
**Impact**: User could use another user's coupon, or use a refunded coupon

### 2. No Server-Side Discount Computation
**Location**: OrderService.java:51
**Issue**: `payAmount = amount` directly, no discount applied
**Impact**: Client could spoof payAmount

### 3. Coupon Return Missing on Cancel/Timeout
**Location**: OrderService.cancelOrder(), OrderTimeoutTask
**Issue**: Coupons consumed by orders are not returned when order is cancelled
**Impact**: Users lose coupons when orders are cancelled

### 4. Frontend Missing Order API
**Location**: frontend-user/src/api/
**Issue**: No order.js module for order operations
**Impact**: Cannot create orders from user frontend

### 5. No Used Status in CouponPurchase
**Location**: CouponPurchase.java
**Issue**: Status only has paid/processing/refunded, missing "used"
**Impact**: Cannot track coupon consumption for orders
