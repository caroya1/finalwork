package com.dianping.common.port;

import com.dianping.common.dto.OrderDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface OrderPort {
    @GetMapping("/internal/orders/user/{userId}")
    List<OrderDTO> listByUser(@PathVariable("userId") Long userId);
}
