package com.dianping.order.service;

import com.dianping.common.exception.BusinessException;
import com.dianping.order.dto.CreateOrderRequest;
import com.dianping.order.entity.Order;
import com.dianping.order.repository.OrderRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OrderService {
    private static final String ORDER_CACHE_PREFIX = "dp:order:";
    private static final String SHOP_LOCK_PREFIX = "dp:lock:shop:";

    private final OrderRepository orderRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public OrderService(OrderRepository orderRepository, RedisTemplate<String, Object> redisTemplate) {
        this.orderRepository = orderRepository;
        this.redisTemplate = redisTemplate;
    }

    public Order createOrder(CreateOrderRequest request) {
        String lockKey = SHOP_LOCK_PREFIX + request.getShopId();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        if (locked == null || !locked) {
            throw new BusinessException("order is busy, please retry");
        }

        try {
            Order order = new Order();
            order.setUserId(request.getUserId());
            order.setShopId(request.getShopId());
            order.setAmount(request.getAmount());
            order.setStatus(1);
            Order saved = orderRepository.save(order);
            redisTemplate.opsForValue().set(ORDER_CACHE_PREFIX + saved.getId(), saved, 300, TimeUnit.SECONDS);
            return saved;
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    public Order getOrder(Long id) {
        Object cached = redisTemplate.opsForValue().get(ORDER_CACHE_PREFIX + id);
        if (cached instanceof Order) {
            return (Order) cached;
        }
        return orderRepository.findById(id).orElse(null);
    }

    public List<Order> listUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}
