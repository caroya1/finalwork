package com.dianping.shop.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.shop.entity.Shop;
import com.dianping.shop.service.ShopService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/shops")
@Validated
public class ShopController {
    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping
    public ApiResponse<Shop> create(@Valid @RequestBody Shop shop) {
        return ApiResponse.ok(shopService.create(shop));
    }

    @GetMapping
    public ApiResponse<List<Shop>> list(@RequestParam("city") String city) {
        return ApiResponse.ok(shopService.listByCity(city));
    }
}
