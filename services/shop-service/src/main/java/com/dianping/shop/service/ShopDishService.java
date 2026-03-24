package com.dianping.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.shop.entity.ShopDish;
import com.dianping.shop.mapper.ShopDishMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopDishService {
    private final ShopDishMapper shopDishMapper;

    public ShopDishService(ShopDishMapper shopDishMapper) {
        this.shopDishMapper = shopDishMapper;
    }

    public List<ShopDish> listByShopId(Long shopId) {
        LambdaQueryWrapper<ShopDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopDish::getShopId, shopId)
               .orderByDesc(ShopDish::getCreatedAt);
        return shopDishMapper.selectList(wrapper);
    }

    public ShopDish addDish(Long shopId, Long userId, ShopDish dish) {
        dish.setShopId(shopId);
        dish.setUserId(userId);
        dish.touchForCreate();
        shopDishMapper.insert(dish);
        return dish;
    }

    public ShopDish updateDish(Long dishId, ShopDish updateData) {
        ShopDish dish = shopDishMapper.selectById(dishId);
        if (dish == null) {
            return null;
        }
        if (updateData.getName() != null) {
            dish.setName(updateData.getName());
        }
        if (updateData.getPrice() != null) {
            dish.setPrice(updateData.getPrice());
        }
        if (updateData.getDescription() != null) {
            dish.setDescription(updateData.getDescription());
        }
        if (updateData.getImageUrl() != null) {
            dish.setImageUrl(updateData.getImageUrl());
        }
        shopDishMapper.updateById(dish);
        return dish;
    }

    public void deleteDish(Long dishId) {
        shopDishMapper.deleteById(dishId);
    }

    public void updateStatus(Long dishId, Integer status) {
        ShopDish dish = shopDishMapper.selectById(dishId);
        if (dish != null) {
            dish.setStatus(status);
            shopDishMapper.updateById(dish);
        }
    }
}
