package com.dianping.shop.service;

import com.dianping.shop.entity.Shop;
import com.dianping.shop.repository.ShopRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopService {
    private final ShopRepository shopRepository;

    public ShopService(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public Shop create(Shop shop) {
        return shopRepository.save(shop);
    }

    public List<Shop> listByCity(String city) {
        return shopRepository.findByCity(city);
    }
}
