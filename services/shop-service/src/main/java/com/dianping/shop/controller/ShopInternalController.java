package com.dianping.shop.controller;

import com.dianping.common.dto.ShopDTO;
import com.dianping.common.dto.ShopSummary;
import com.dianping.common.port.ShopPort;
import com.dianping.shop.service.ShopService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/shops")
public class ShopInternalController {
    private final ShopPort shopPort;
    private final ShopService shopService;

    public ShopInternalController(ShopPort shopPort, ShopService shopService) {
        this.shopPort = shopPort;
        this.shopService = shopService;
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
    
    /**
     * 根据ID获取店铺详情（供推荐服务调用）
     */
    @GetMapping("/{id}")
    public ShopDTO getById(@PathVariable("id") Long id) {
        return shopService.getShopById(id);
    }
    
    /**
     * 根据城市获取店铺列表（供推荐服务调用）
     */
    @GetMapping("/listByCity")
    public List<ShopDTO> listByCity(@RequestParam("city") String city) {
        return shopService.listByCity(city);
    }
}
