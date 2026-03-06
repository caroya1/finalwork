package com.dianping.shop.service;

import com.dianping.common.dto.ShopSummary;
import com.dianping.common.port.ShopPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopPortImpl implements ShopPort {
    private final ShopService shopService;

    public ShopPortImpl(ShopService shopService) {
        this.shopService = shopService;
    }

    @Override
    public ShopSummary getSummary(Long id) {
        return shopService.getSummary(id);
    }

    @Override
    public List<ShopSummary> listSummaries(String city, String category) {
        return shopService.listSummaries(city, category);
    }
}
