package com.dianping.order.controller;

import com.dianping.common.dto.OrderDTO;
import com.dianping.order.entity.Order;
import com.dianping.order.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/orders")
public class InternalOrderController {
    private final OrderService orderService;

    public InternalOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/user/{userId}")
    public List<OrderDTO> listByUser(@PathVariable("userId") Long userId) {
        List<Order> orders = orderService.listUserOrders(userId);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setShopId(order.getShopId());
        dto.setCouponId(order.getCouponId());
        dto.setCouponPurchaseId(order.getCouponPurchaseId());
        dto.setAmount(order.getAmount());
        dto.setPayAmount(order.getPayAmount());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setStatus(order.getStatus());
        dto.setRemark(order.getRemark());
        dto.setPaidAt(order.getPaidAt());
        dto.setVerifiedAt(order.getVerifiedAt());
        dto.setCancelledAt(order.getCancelledAt());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }
}
