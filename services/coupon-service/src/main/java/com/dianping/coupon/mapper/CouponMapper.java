package com.dianping.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dianping.coupon.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    /**
     * 原子性扣减库存
     * @param couponId 优惠券ID
     * @return 更新行数，大于0表示扣减成功
     */
    @Update("UPDATE dp_coupon SET remaining_stock = remaining_stock - 1, updated_at = NOW() " +
            "WHERE id = #{couponId} AND remaining_stock > 0")
    int decrementStock(Long couponId);
}
