package com.dianping.order.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.order.dto.CreateOrderRequest;
import com.dianping.order.dto.OrderQueryRequest;
import com.dianping.order.dto.PageResult;
import com.dianping.order.entity.Order;
import com.dianping.order.service.OrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<Order> create(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok(orderService.createOrder(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<Order> get(@PathVariable("id") Long id) {
        return ApiResponse.ok(orderService.getOrder(id));
    }

    @GetMapping("/no/{orderNo}")
    public ApiResponse<Order> getByNo(@PathVariable("orderNo") String orderNo) {
        return ApiResponse.ok(orderService.getOrderByNo(orderNo));
    }

    @GetMapping
    public ApiResponse<List<Order>> listByUser(@RequestParam("userId") Long userId) {
        return ApiResponse.ok(orderService.listUserOrders(userId));
    }

    @PostMapping("/query")
    public ApiResponse<PageResult<Order>> query(@RequestBody OrderQueryRequest request) {
        return ApiResponse.ok(orderService.queryOrders(request));
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<Order> pay(@PathVariable("id") Long orderId, @RequestParam Integer payAmount) {
        return ApiResponse.ok(orderService.payOrder(orderId, payAmount));
    }

    @PostMapping("/{id}/verify")
    public ApiResponse<Order> verify(@PathVariable("id") Long orderId, @RequestParam(required = false) Long operatorId) {
        return ApiResponse.ok(orderService.verifyOrder(orderId, operatorId));
    }

    @PostMapping("/{id}/refund")
    public ApiResponse<Order> refund(@PathVariable("id") Long orderId, @RequestParam(required = false) String reason) {
        return ApiResponse.ok(orderService.refundOrder(orderId, reason));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Order> cancel(@PathVariable("id") Long orderId, @RequestParam(required = false) String reason) {
        return ApiResponse.ok(orderService.cancelOrder(orderId, reason));
    }

    @GetMapping("/shop/{shopId}")
    public ApiResponse<List<Order>> listByShop(
            @PathVariable("shopId") Long shopId,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.ok(orderService.listShopOrders(shopId, status));
    }

    @GetMapping("/shop/{shopId}/stats")
    public ApiResponse<OrderStats> getShopStats(@PathVariable("shopId") Long shopId) {
        OrderStats stats = new OrderStats();
        stats.setOrderCount(orderService.countPaidOrdersByShop(shopId));
        stats.setRevenue(orderService.sumRevenueByShop(shopId));
        return ApiResponse.ok(stats);
    }

    public static class OrderStats {
        private Integer orderCount;
        private Integer revenue;

        public Integer getOrderCount() {
            return orderCount;
        }

        public void setOrderCount(Integer orderCount) {
            this.orderCount = orderCount;
        }

        public Integer getRevenue() {
            return revenue;
        }

        public void setRevenue(Integer revenue) {
            this.revenue = revenue;
        }
    }
}
