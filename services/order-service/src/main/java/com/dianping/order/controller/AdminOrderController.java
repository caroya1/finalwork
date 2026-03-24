package com.dianping.order.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.order.dto.OrderQueryRequest;
import com.dianping.order.dto.PageResult;
import com.dianping.order.entity.Order;
import com.dianping.order.service.OrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/query")
    public ApiResponse<PageResult<Order>> query(@RequestBody OrderQueryRequest request) {
        return ApiResponse.ok(orderService.queryOrders(request));
    }

    @GetMapping("/today-count")
    public ApiResponse<Integer> todayCount() {
        return ApiResponse.ok(orderService.countTodayOrders());
    }

    @GetMapping("/export")
    public void export(@RequestParam(required = false) Long userId,
                       @RequestParam(required = false) Long shopId,
                       @RequestParam(required = false) Integer status,
                       HttpServletResponse response) throws IOException {
        OrderQueryRequest request = new OrderQueryRequest();
        request.setUserId(userId);
        request.setShopId(shopId);
        request.setStatus(status);
        request.setPage(1);
        request.setPageSize(10000);

        PageResult<Order> result = orderService.queryOrders(request);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=orders.csv");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        writer.write("\uFEFF");
        writer.println("订单号,用户ID,店铺ID,订单金额,实付金额,状态,创建时间,支付时间,核销时间");

        for (Order order : result.getRecords()) {
            String statusText = getStatusText(order.getStatus());
            writer.println(String.format("%s,%d,%d,%d,%d,%s,%s,%s,%s",
                    order.getOrderNo(),
                    order.getUserId(),
                    order.getShopId(),
                    order.getAmount(),
                    order.getPayAmount() != null ? order.getPayAmount() : 0,
                    statusText,
                    order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_FORMAT) : "",
                    order.getPaidAt() != null ? order.getPaidAt().format(DATE_FORMAT) : "",
                    order.getVerifiedAt() != null ? order.getVerifiedAt().format(DATE_FORMAT) : ""
            ));
        }
        writer.flush();
    }

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "已核销";
            case 3: return "已退款";
            case 4: return "已取消";
            default: return "未知";
        }
    }
}
