package com.dianping.order.service;

import com.dianping.common.exception.BusinessException;
import com.dianping.order.dto.CreateOrderRequest;
import com.dianping.order.entity.Order;
import com.dianping.order.enums.OrderStatus;
import com.dianping.order.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void createOrder_Success() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);
        request.setShopId(1L);
        request.setAmount(100);
        request.setRemark("测试订单");

        when(valueOperations.setIfAbsent(anyString(), any(), anyLong(), any()))
                .thenReturn(true);
        when(orderMapper.insert(any(Order.class))).thenReturn(1);

        // When
        Order result = orderService.createOrder(request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(1L, result.getShopId());
        assertEquals(100, result.getAmount());
        assertEquals(OrderStatus.PENDING_PAYMENT.getCode(), result.getStatus());
        assertTrue(result.getOrderNo().startsWith("ORD"));
        
        verify(orderMapper).insert(any(Order.class));
        verify(valueOperations).set(anyString(), any(Order.class), anyLong(), any());
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void createOrder_LockFailed() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest();
        request.setShopId(1L);

        when(valueOperations.setIfAbsent(anyString(), any(), anyLong(), any()))
                .thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(request);
        });
        assertEquals("系统繁忙，请稍后重试", exception.getMessage());
    }

    @Test
    void payOrder_Success() {
        // Given
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setAmount(100);

        when(valueOperations.get(anyString())).thenReturn(null);
        when(orderMapper.selectById(orderId)).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        // When
        Order result = orderService.payOrder(orderId, 100);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.PAID.getCode(), result.getStatus());
        assertEquals(100, result.getPayAmount());
        assertNotNull(result.getPaidAt());
        
        verify(orderMapper).updateById(any(Order.class));
    }

    @Test
    void payOrder_OrderNotFound() {
        // Given
        Long orderId = 1L;
        when(valueOperations.get(anyString())).thenReturn(null);
        when(orderMapper.selectById(orderId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.payOrder(orderId, 100);
        });
        assertEquals("订单不存在", exception.getMessage());
    }

    @Test
    void payOrder_InvalidStatus() {
        // Given
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PAID.getCode());

        when(valueOperations.get(anyString())).thenReturn(null);
        when(orderMapper.selectById(orderId)).thenReturn(order);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.payOrder(orderId, 100);
        });
        assertEquals("订单状态不允许支付", exception.getMessage());
    }

    @Test
    void verifyOrder_Success() {
        // Given
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PAID.getCode());

        when(valueOperations.get(anyString())).thenReturn(null);
        when(orderMapper.selectById(orderId)).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        // When
        Order result = orderService.verifyOrder(orderId, 2L);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.VERIFIED.getCode(), result.getStatus());
        assertNotNull(result.getVerifiedAt());
    }

    @Test
    void refundOrder_Success() {
        // Given
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PAID.getCode());
        order.setRemark("原备注");

        when(valueOperations.get(anyString())).thenReturn(null);
        when(orderMapper.selectById(orderId)).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        // When
        Order result = orderService.refundOrder(orderId, "用户申请退款");

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.REFUNDED.getCode(), result.getStatus());
        assertTrue(result.getRemark().contains("用户申请退款"));
    }

    @Test
    void cancelOrder_Success() {
        // Given
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());

        when(valueOperations.get(anyString())).thenReturn(null);
        when(orderMapper.selectById(orderId)).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        // When
        Order result = orderService.cancelOrder(orderId, "用户主动取消");

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED.getCode(), result.getStatus());
        assertNotNull(result.getCancelledAt());
    }

    @Test
    void getOrder_FromCache() {
        // Given
        Long orderId = 1L;
        Order cachedOrder = new Order();
        cachedOrder.setId(orderId);
        cachedOrder.setOrderNo("ORD123");

        when(valueOperations.get(anyString())).thenReturn(cachedOrder);

        // When
        Order result = orderService.getOrder(orderId);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals("ORD123", result.getOrderNo());
        verify(orderMapper, never()).selectById(any());
    }

    @Test
    void getOrder_FromDatabase() {
        // Given
        Long orderId = 1L;
        Order dbOrder = new Order();
        dbOrder.setId(orderId);
        dbOrder.setOrderNo("ORD123");

        when(valueOperations.get(anyString())).thenReturn(null);
        when(orderMapper.selectById(orderId)).thenReturn(dbOrder);

        // When
        Order result = orderService.getOrder(orderId);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        verify(orderMapper).selectById(orderId);
    }

    @Test
    void cancelExpiredOrders_Success() {
        // Given
        Order expiredOrder = new Order();
        expiredOrder.setId(1L);
        expiredOrder.setStatus(OrderStatus.PENDING_PAYMENT.getCode());

        when(orderMapper.findExpiredOrders(any(), any()))
                .thenReturn(Arrays.asList(expiredOrder));
        when(valueOperations.get(anyString())).thenReturn(null);
        when(orderMapper.selectById(1L)).thenReturn(expiredOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        // When
        int count = orderService.cancelExpiredOrders();

        // Then
        assertEquals(1, count);
        verify(orderMapper).updateById(any(Order.class));
    }
}
