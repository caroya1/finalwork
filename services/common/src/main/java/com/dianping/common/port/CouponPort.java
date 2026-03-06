package com.dianping.common.port;

import com.dianping.common.dto.UserCouponView;

import java.util.List;

public interface CouponPort {
    List<UserCouponView> listByUser(Long userId);
}
