package com.dianping.order.service;

import com.dianping.common.exception.BusinessException;
import com.dianping.order.dto.CreateOrderRequest;
import com.dianping.order.dto.OrderQueryRequest;
import com.dianping.order.dto.PageResult;
import com.dianping.order.entity.Order;
import com.dianping.order.enums.OrderStatus;
import com.dianping.order.mapper.OrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class OrderService {
    private static final String ORDER_CACHE_PREFIX = "dp:order:";
    private static final String SHOP_LOCK_PREFIX = "dp:lock:shop:";
    private static final int ORDER_EXPIRE_MINUTES = 30;

    private final OrderMapper orderMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public OrderService(OrderMapper orderMapper, RedisTemplate<String, Object> redisTemplate) {
        this.orderMapper = orderMapper;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        String lockKey = SHOP_LOCK_PREFIX + request.getShopId();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        if (locked == null || !locked) {
            throw new BusinessException("系统繁忙，请稍后重试");
        }

        try {
            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setUserId(request.getUserId());
            order.setShopId(request.getShopId());
            order.setCouponId(request.getCouponId());
            order.setAmount(request.getAmount());
            order.setPayAmount(request.getAmount());
            order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
            order.setRemark(request.getRemark());
            order.touchForCreate();
            orderMapper.insert(order);

            redisTemplate.opsForValue().set(ORDER_CACHE_PREFIX + order.getId(), order, 300, TimeUnit.SECONDS);
            return order;
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD" + timestamp + random;
    }

    @Transactional
    public Order payOrder(Long orderId, Integer payAmount) {
        Order order = getOrder(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        OrderStatus currentStatus = order.getStatusEnum();
        if (!currentStatus.canTransitionTo(OrderStatus.PAID)) {
            throw new BusinessException("订单状态不允许支付");
        }

        order.setStatus(OrderStatus.PAID.getCode());
        order.setPayAmount(payAmount);
        order.setPaidAt(LocalDateTime.now());
        order.touchForUpdate();
        orderMapper.updateById(order);

        redisTemplate.opsForValue().set(ORDER_CACHE_PREFIX + order.getId(), order, 300, TimeUnit.SECONDS);
        return order;
    }

    @Transactional
    public Order verifyOrder(Long orderId, Long operatorId) {
        Order order = getOrder(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        OrderStatus currentStatus = order.getStatusEnum();
        if (!currentStatus.canTransitionTo(OrderStatus.VERIFIED)) {
            throw new BusinessException("订单状态不允许核销");
        }

        order.setStatus(OrderStatus.VERIFIED.getCode());
        order.setVerifiedAt(LocalDateTime.now());
        order.touchForUpdate();
        orderMapper.updateById(order);

        redisTemplate.opsForValue().set(ORDER_CACHE_PREFIX + order.getId(), order, 300, TimeUnit.SECONDS);
        return order;
    }

    @Transactional
    public Order refundOrder(Long orderId, String reason) {
        Order order = getOrder(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        OrderStatus currentStatus = order.getStatusEnum();
        if (!currentStatus.canTransitionTo(OrderStatus.REFUNDED)) {
            throw new BusinessException("订单状态不允许退款");
        }

        order.setStatus(OrderStatus.REFUNDED.getCode());
        order.setRemark(reason != null ? order.getRemark() + " [退款原因:" + reason + "]" : order.getRemark());
        order.touchForUpdate();
        orderMapper.updateById(order);

        redisTemplate.opsForValue().set(ORDER_CACHE_PREFIX + order.getId(), order, 300, TimeUnit.SECONDS);
        return order;
    }

    @Transactional
    public Order cancelOrder(Long orderId, String reason) {
        Order order = getOrder(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        OrderStatus currentStatus = order.getStatusEnum();
        if (!currentStatus.canTransitionTo(OrderStatus.CANCELLED)) {
            throw new BusinessException("订单状态不允许取消");
        }

        order.setStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelledAt(LocalDateTime.now());
        order.setRemark(reason != null ? order.getRemark() + " [取消原因:" + reason + "]" : order.getRemark());
        order.touchForUpdate();
        orderMapper.updateById(order);

        redisTemplate.opsForValue().set(ORDER_CACHE_PREFIX + order.getId(), order, 300, TimeUnit.SECONDS);
        return order;
    }

    public Order getOrder(Long id) {
        Object cached = redisTemplate.opsForValue().get(ORDER_CACHE_PREFIX + id);
        if (cached instanceof Order) {
            return (Order) cached;
        }
        return orderMapper.selectById(id);
    }

    public Order getOrderByNo(String orderNo) {
        return orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
    }

    public List<Order> listUserOrders(Long userId) {
        return orderMapper.selectList(new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId).orderByDesc(Order::getCreatedAt));
    }

    public PageResult<Order> queryOrders(OrderQueryRequest request) {
        Page<Order> page = new Page<>(request.getPage(), request.getPageSize());
        return PageResult.of(orderMapper.queryPage(page, request));
    }

    public List<Order> listShopOrders(Long shopId, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId)
                .orderByDesc(Order::getCreatedAt);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        return orderMapper.selectList(wrapper);
    }

    public int cancelExpiredOrders() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(ORDER_EXPIRE_MINUTES);
        List<Order> expiredOrders = orderMapper.findExpiredOrders(OrderStatus.PENDING_PAYMENT.getCode(), expireTime);
        int count = 0;
        for (Order order : expiredOrders) {
            try {
                cancelOrder(order.getId(), "订单超时自动取消");
                count++;
            } catch (Exception e) {
            }
        }
        return count;
    }

    public Integer countPaidOrdersByShop(Long shopId) {
        return orderMapper.countPaidOrdersByShop(shopId);
    }

    public Integer sumRevenueByShop(Long shopId) {
        return orderMapper.sumRevenueByShop(shopId);
    }

    public Integer countTodayOrders() {
        return orderMapper.countTodayOrders();
    }
}
