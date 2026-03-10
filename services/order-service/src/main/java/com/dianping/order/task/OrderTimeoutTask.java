package com.dianping.order.task;

import com.dianping.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderTimeoutTask {
    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutTask.class);

    private final OrderService orderService;

    public OrderTimeoutTask(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedRate = 60000)
    public void cancelExpiredOrders() {
        log.info("开始执行订单超时取消任务");
        try {
            int count = orderService.cancelExpiredOrders();
            log.info("订单超时取消任务完成，取消了 {} 个订单", count);
        } catch (Exception e) {
            log.error("订单超时取消任务执行失败", e);
        }
    }
}
