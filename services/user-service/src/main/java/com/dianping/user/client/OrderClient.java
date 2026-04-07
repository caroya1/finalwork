package com.dianping.user.client;

import com.dianping.common.dto.OrderDTO;
import com.dianping.common.port.OrderPort;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "order-service")
public interface OrderClient extends OrderPort {
    @Override
    @GetMapping("/internal/orders/user/{userId}")
    List<OrderDTO> listByUser(@PathVariable("userId") Long userId);
}
