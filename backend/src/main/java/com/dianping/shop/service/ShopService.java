package com.dianping.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.shop.entity.Shop;
import com.dianping.shop.mapper.ShopMapper;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopService {
    private final ShopMapper shopMapper;

    public ShopService(ShopMapper shopMapper) {
        this.shopMapper = shopMapper;
    }

    public Shop getById(Long id) {
        return shopMapper.selectById(id);
    }

    public Shop create(Shop shop) {
        shop.touchForCreate();
        shopMapper.insert(shop);
        return shop;
    }

    public List<Shop> list(String city, String category) {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        if (city != null && !city.trim().isEmpty()) {
            wrapper.eq(Shop::getCity, city.trim());
        }
        if (category != null && !category.trim().isEmpty()) {
            List<String> categoryList = Arrays.stream(category.split(","))
                    .map(String::trim)
                    .filter(item -> !item.isEmpty())
                    .collect(Collectors.toList());
            if (categoryList.size() == 1) {
                wrapper.eq(Shop::getCategory, categoryList.get(0));
            } else if (!categoryList.isEmpty()) {
                wrapper.in(Shop::getCategory, categoryList);
            }
        }
        wrapper.orderByDesc(Shop::getRating).orderByDesc(Shop::getCreatedAt);
        return shopMapper.selectList(wrapper);
    }
}
