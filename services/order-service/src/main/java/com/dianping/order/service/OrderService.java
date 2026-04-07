package com.dianping.order.service;

import com.dianping.common.dto.ConsumeCouponResult;
import com.dianping.common.exception.BusinessException;
import com.dianping.order.client.CouponClient;
import com.dianping.order.client.UserClient;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final String ORDER_CACHE_PREFIX = "dp:order:";
    private static final String SHOP_LOCK_PREFIX = "dp:lock:shop:";
    private static final int ORDER_EXPIRE_MINUTES = 30;

    private final OrderMapper orderMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CouponClient couponClient;
    private final UserClient userClient;

    public OrderService(OrderMapper orderMapper, RedisTemplate<String, Object> redisTemplate,
                        CouponClient couponClient, UserClient userClient) {
        this.orderMapper = orderMapper;
        this.redisTemplate = redisTemplate;
        this.couponClient = couponClient;
        this.userClient = userClient;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        // 参数校验
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (request.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (request.getShopId() == null) {
            throw new BusinessException("店铺ID不能为空");
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new BusinessException("订单金额必须大于0");
        }

        String lockKey = SHOP_LOCK_PREFIX + request.getShopId();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        if (locked == null || !locked) {
            throw new BusinessException("系统繁忙，请稍后重试");
        }

        try {
            Order order = new Order();
            String orderNo = generateOrderNo();
            order.setOrderNo(orderNo);
            order.setUserId(request.getUserId());
            order.setShopId(request.getShopId());
            order.setCouponId(request.getCouponId());
            order.setAmount(request.getAmount());

            Integer payAmount;
            Long couponPurchaseId = null;
            Integer discountAmount = null;

            if (request.getCouponId() != null) {
                try {
                    ConsumeCouponResult result = couponClient.consumeCoupon(
                            request.getUserId(),
                            request.getCouponId(),
                            request.getShopId(),
                            orderNo
                    );

                    if (result == null) {
                        throw new BusinessException("优惠券核销失败");
                    }

                    couponPurchaseId = result.getPurchaseId();
                    discountAmount = yuanToCents(result.getDiscountAmount());
                    payAmount = Math.max(0, request.getAmount() - discountAmount);
                } catch (BusinessException e) {
                    throw e;
                } catch (Exception e) {
                    log.error("优惠券核销异常: {}", e.getMessage());
                    throw new BusinessException("优惠券核销失败: " + e.getMessage());
                }
            } else {
                payAmount = request.getAmount();
            }

            // 确保支付金额不为负数
            if (payAmount < 0) {
                payAmount = 0;
            }

            order.setCouponPurchaseId(couponPurchaseId);
            order.setDiscountAmount(discountAmount);
            order.setPayAmount(payAmount);
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

    private Integer yuanToCents(BigDecimal yuan) {
        if (yuan == null) {
            return 0;
        }
        return yuan.multiply(new BigDecimal("100")).intValue();
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

        // 扣减用户余额（前端传入的是元，直接扣减）
        try {
            userClient.deductBalance(order.getUserId(), new BigDecimal(payAmount));
        } catch (Exception e) {
            log.error("扣减余额失败: userId={}, amount={}, error={}", order.getUserId(), payAmount, e.getMessage());
            throw new BusinessException("余额不足或扣减失败");
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

        if (order.getCouponPurchaseId() != null) {
            try {
                couponClient.returnCoupon(order.getCouponPurchaseId(), order.getOrderNo(), reason);
            } catch (Exception e) {
                log.warn("Failed to return coupon for order {}: {}", orderId, e.getMessage());
            }
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
