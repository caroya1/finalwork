package com.dianping.common.service;

import com.dianping.common.dto.ShopSummary;
import com.dianping.shop.service.ShopService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopFacade {
    private final ShopService shopService;

    public ShopFacade(ShopService shopService) {
        this.shopService = shopService;
    }

    public ShopSummary getSummary(Long id) {
        return shopService.getSummary(id);
    }

    public ShopSummary getPlainSummary(Long id) {
        com.dianping.shop.entity.Shop shop = shopService.getPlainById(id);
        if (shop == null) {
            return null;
        }
        return new ShopSummary(
                shop.getId(),
                shop.getName(),
                shop.getCategory(),
                shop.getCity(),
                shop.getRating(),
                shop.getAddress()
        );
    }

    public List<ShopSummary> listSummaries(String city, String category) {
        return shopService.listSummaries(city, category);
    }
}
