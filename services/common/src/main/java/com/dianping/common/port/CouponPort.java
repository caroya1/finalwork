package com.dianping.common.port;

import com.dianping.common.dto.ConsumeCouponResult;
import com.dianping.common.dto.UserCouponView;

import java.util.List;

public interface CouponPort {
    List<UserCouponView> listByUser(Long userId);

    ConsumeCouponResult consumeCoupon(Long userId, Long couponId, Long shopId, String orderNo);

    void returnCoupon(Long purchaseId, String orderNo, String reason);
}
