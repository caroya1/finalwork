package com.dianping.order.service;

import com.dianping.common.exception.BusinessException;
import com.dianping.order.dto.CreateOrderRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.order.entity.Order;
import com.dianping.order.mapper.OrderMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OrderService {
    private static final String ORDER_CACHE_PREFIX = "dp:order:";
    private static final String SHOP_LOCK_PREFIX = "dp:lock:shop:";

    private final OrderMapper orderMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public OrderService(OrderMapper orderMapper, RedisTemplate<String, Object> redisTemplate) {
        this.orderMapper = orderMapper;
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
            order.touchForCreate();
            orderMapper.insert(order);
            Order saved = order;
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
        return orderMapper.selectById(id);
    }

    public List<Order> listUserOrders(Long userId) {
        return orderMapper.selectList(new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId));
    }
}
