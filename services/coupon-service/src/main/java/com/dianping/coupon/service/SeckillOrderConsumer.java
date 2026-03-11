package com.dianping.coupon.service;

import com.dianping.coupon.dto.SeckillOrderMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * 秒杀订单消费者
 */
@Service
public class SeckillOrderConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(SeckillOrderConsumer.class);
    
    private final SeckillOrderHandler seckillOrderHandler;

    public SeckillOrderConsumer(SeckillOrderHandler seckillOrderHandler) {
        this.seckillOrderHandler = seckillOrderHandler;
    }

    @RabbitListener(queues = "${app.seckill.queue-name:dp.seckill.coupon.queue}")
    public void handleSeckillOrder(SeckillOrderMessage message) {
        if (message == null) {
            logger.warn("收到空的秒杀订单消息");
            return;
        }
        
        Long couponId = message.getCouponId();
        Long userId = message.getUserId();
        
        logger.info("收到秒杀订单消息, couponId={}, userId={}", couponId, userId);
        
        try {
            seckillOrderHandler.handle(couponId, userId);
            logger.info("秒杀订单处理完成, couponId={}, userId={}", couponId, userId);
        } catch (Exception e) {
            logger.error("秒杀订单处理失败, couponId={}, userId={}", couponId, userId, e);
            // 这里可以添加重试逻辑或死信队列处理
            throw e;
        }
    }
}
