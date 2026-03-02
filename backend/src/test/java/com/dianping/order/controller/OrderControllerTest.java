package com.dianping.order.controller;

import com.dianping.order.dto.CreateOrderRequest;
import com.dianping.order.entity.Order;
import com.dianping.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void createOrderReturnsOk() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setShopId(2L);
        order.setAmount(100);
        order.setStatus(1);

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"shopId\":2,\"amount\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getOrderReturnsOk() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setShopId(2L);
        order.setAmount(100);
        order.setStatus(1);

        when(orderService.getOrder(1L)).thenReturn(order);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void listOrdersReturnsOk() throws Exception {
        when(orderService.listUserOrders(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/orders").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
