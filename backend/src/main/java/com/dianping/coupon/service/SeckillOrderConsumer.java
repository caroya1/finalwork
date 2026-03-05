package com.dianping.coupon.service;

import com.dianping.coupon.dto.SeckillOrderMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class SeckillOrderConsumer {
    private final CouponService couponService;

    public SeckillOrderConsumer(CouponService couponService) {
        this.couponService = couponService;
    }

    @RabbitListener(queues = "${app.seckill.queue-name:dp.seckill.coupon.queue}")
    public void handleSeckillOrder(SeckillOrderMessage message) {
        if (message == null) {
            return;
        }
        couponService.handleSeckillOrder(message.getCouponId(), message.getUserId());
    }
}
