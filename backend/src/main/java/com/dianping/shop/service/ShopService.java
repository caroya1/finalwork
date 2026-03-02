package com.dianping.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.shop.entity.Shop;
import com.dianping.shop.mapper.ShopMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopService {
    private final ShopMapper shopMapper;

    public ShopService(ShopMapper shopMapper) {
        this.shopMapper = shopMapper;
    }

    public Shop create(Shop shop) {
        shop.touchForCreate();
        shopMapper.insert(shop);
        return shop;
    }

    public List<Shop> listByCity(String city) {
        return shopMapper.selectList(new LambdaQueryWrapper<Shop>().eq(Shop::getCity, city));
    }
}
