package com.dianping.shop.controller;

import com.dianping.common.dto.ShopSummary;
import com.dianping.common.port.ShopPort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/shops")
public class ShopInternalController {
    private final ShopPort shopPort;

    public ShopInternalController(ShopPort shopPort) {
        this.shopPort = shopPort;
    }

    @GetMapping("/{id}/summary")
    public ShopSummary getSummary(@PathVariable("id") Long id) {
        return shopPort.getSummary(id);
    }

    @GetMapping("/summaries")
    public List<ShopSummary> listSummaries(@RequestParam(value = "city", required = false) String city,
                                           @RequestParam(value = "category", required = false) String category) {
        return shopPort.listSummaries(city, category);
    }
}
