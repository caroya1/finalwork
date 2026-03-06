package com.dianping.order.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.order.dto.CreateOrderRequest;
import com.dianping.order.entity.Order;
import com.dianping.order.service.OrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    @GetMapping
    public ApiResponse<List<Order>> listByUser(@RequestParam("userId") Long userId) {
        return ApiResponse.ok(orderService.listUserOrders(userId));
    }
}
