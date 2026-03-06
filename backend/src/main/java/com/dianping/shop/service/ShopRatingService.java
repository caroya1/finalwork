package com.dianping.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.exception.BusinessException;
import com.dianping.shop.dto.ShopRatingRequest;
import com.dianping.shop.entity.Shop;
import com.dianping.shop.entity.ShopRating;
import com.dianping.shop.mapper.ShopMapper;
import com.dianping.shop.mapper.ShopRatingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShopRatingService {
    private final ShopRatingMapper shopRatingMapper;
    private final ShopMapper shopMapper;
    private final ShopService shopService;

    public ShopRatingService(ShopRatingMapper shopRatingMapper, ShopMapper shopMapper, ShopService shopService) {
        this.shopRatingMapper = shopRatingMapper;
        this.shopMapper = shopMapper;
        this.shopService = shopService;
    }

    @Transactional(transactionManager = "shopTransactionManager")
    public void rate(Long shopId, Long userId, ShopRatingRequest request) {
        if (request.getRating() == null || request.getRating() < 0 || request.getRating() > 5) {
            throw new BusinessException("rating must be between 0 and 5");
        }
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException("shop not found");
        }

        ShopRating existing = shopRatingMapper.selectOne(new LambdaQueryWrapper<ShopRating>()
                .eq(ShopRating::getShopId, shopId)
                .eq(ShopRating::getUserId, userId));
        if (existing == null) {
            ShopRating rating = new ShopRating();
            rating.setShopId(shopId);
            rating.setUserId(userId);
            rating.setRating(request.getRating());
            rating.touchForCreate();
            shopRatingMapper.insert(rating);
        } else {
            existing.setRating(request.getRating());
            existing.touchForUpdate();
            shopRatingMapper.updateById(existing);
        }

        updateShopAverage(shopId);
    }

    public double getAverage(Long shopId) {
        List<ShopRating> ratings = shopRatingMapper.selectList(new LambdaQueryWrapper<ShopRating>()
                .eq(ShopRating::getShopId, shopId));
        if (ratings.isEmpty()) {
            return 0.0;
        }
        double sum = ratings.stream().mapToDouble(ShopRating::getRating).sum();
        return Math.round((sum / ratings.size()) * 100.0) / 100.0;
    }

    private void updateShopAverage(Long shopId) {
        double avg = getAverage(shopId);
        Shop shop = shopMapper.selectById(shopId);
        if (shop != null) {
            shop.setRating(avg);
            shop.touchForUpdate();
            shopMapper.updateById(shop);
            shopService.invalidateCache(shopId, shop.getCity(), shop.getCategory());
        }
    }
}
