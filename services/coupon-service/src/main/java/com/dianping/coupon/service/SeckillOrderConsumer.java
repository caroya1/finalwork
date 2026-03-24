package com.dianping.coupon.service;

import com.dianping.coupon.dto.SeckillOrderMessage;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 秒杀订单消费者
 * 使用手动确认模式，确保消息处理成功后才确认
 */
@Service
public class SeckillOrderConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(SeckillOrderConsumer.class);
    
    private final SeckillOrderHandler seckillOrderHandler;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public SeckillOrderConsumer(SeckillOrderHandler seckillOrderHandler) {
        this.seckillOrderHandler = seckillOrderHandler;
    }

    @RabbitListener(
        queues = "${app.seckill.queue-name:dp.seckill.coupon.queue}",
        concurrency = "10-20",
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleSeckillOrder(SeckillOrderMessage message, Channel channel, Message amqpMessage) throws IOException {
        if (message == null) {
            logger.warn("收到空的秒杀订单消息");
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        
        Long couponId = message.getCouponId();
        Long userId = message.getUserId();
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        
        logger.info("收到秒杀订单消息, couponId={}, userId={}", couponId, userId);
        
        try {
            seckillOrderHandler.handle(couponId, userId);
            logger.info("秒杀订单处理完成, couponId={}, userId={}", couponId, userId);
            // 处理成功，确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            logger.error("秒杀订单处理失败, couponId={}, userId={}, error={}", couponId, userId, e.getMessage());
            // 处理失败，拒绝消息并重新入队（最多重试 3 次）
            Integer retryCount = (Integer) amqpMessage.getMessageProperties().getHeaders().get("x-retry-count");
            if (retryCount == null) retryCount = 0;
            
            if (retryCount < 3) {
                logger.warn("消息处理失败，重新入队进行重试, couponId={}, userId={}, retryCount={}", couponId, userId, retryCount + 1);
                amqpMessage.getMessageProperties().setHeader("x-retry-count", retryCount + 1);
                channel.basicReject(deliveryTag, true);
            } else {
                logger.error("消息重试次数超过限制，放弃处理, couponId={}, userId={}", couponId, userId);
                channel.basicReject(deliveryTag, false);
            }
        }
    }
}
