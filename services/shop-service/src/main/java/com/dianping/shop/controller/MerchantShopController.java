package com.dianping.shop.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.common.exception.BusinessException;
import com.dianping.shop.dto.DishCreateRequest;
import com.dianping.shop.dto.ShopCreateRequest;
import com.dianping.shop.entity.Shop;
import com.dianping.shop.entity.ShopDish;
import com.dianping.shop.service.ShopService;
import com.dianping.shop.service.ShopDishService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/merchant/shops")
@Validated
public class MerchantShopController {
    private final ShopService shopService;
    private final ShopDishService shopDishService;

    public MerchantShopController(ShopService shopService, ShopDishService shopDishService) {
        this.shopService = shopService;
        this.shopDishService = shopDishService;
    }

    @PostMapping
    public ApiResponse<Shop> createShop(
            @RequestHeader("X-Merchant-Id") Long merchantId,
            @Valid @RequestBody ShopCreateRequest request) {
        Shop shop = new Shop();
        shop.setName(request.getName());
        shop.setCategory(request.getCategory());
        shop.setTags(request.getTags());
        shop.setCity(request.getCity());
        shop.setAddress(request.getAddress());
        shop.setLongitude(request.getLongitude());
        shop.setLatitude(request.getLatitude());
        shop.setImageUrl(request.getImageUrl());
        shop.setImages(request.getImages());
        shop.setBusinessHours(request.getBusinessHours());
        shop.setContactPhone(request.getContactPhone());
        shop.setMerchantId(merchantId);
        return ApiResponse.ok(shopService.create(shop));
    }

    @GetMapping
    public ApiResponse<List<Shop>> listMyShops(@RequestHeader("X-Merchant-Id") Long merchantId) {
        return ApiResponse.ok(shopService.listByMerchantId(merchantId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Shop> getShop(@PathVariable("id") Long id,
                                     @RequestHeader("X-Merchant-Id") Long merchantId) {
        Shop shop = shopService.getById(id);
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }
        if (!merchantId.equals(shop.getMerchantId())) {
            throw new BusinessException("无权访问此店铺");
        }
        return ApiResponse.ok(shop);
    }

    @PutMapping("/{id}")
    public ApiResponse<Shop> updateShop(@PathVariable("id") Long id,
                                        @RequestHeader("X-Merchant-Id") Long merchantId,
                                        @RequestBody Shop updateData) {
        Shop shop = shopService.getById(id);
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }
        if (!merchantId.equals(shop.getMerchantId())) {
            throw new BusinessException("无权修改此店铺");
        }
        return ApiResponse.ok(shopService.update(id, updateData));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteShop(@PathVariable("id") Long id,
                                        @RequestHeader("X-Merchant-Id") Long merchantId) {
        Shop shop = shopService.getById(id);
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }
        if (!merchantId.equals(shop.getMerchantId())) {
            throw new BusinessException("无权删除此店铺");
        }
        shopService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{shopId}/dishes")
    public ApiResponse<List<ShopDish>> listDishes(@PathVariable("shopId") Long shopId,
                                                   @RequestHeader("X-Merchant-Id") Long merchantId) {
        Shop shop = shopService.getById(shopId);
        if (shop == null || !merchantId.equals(shop.getMerchantId())) {
            throw new BusinessException("无权访问此店铺");
        }
        return ApiResponse.ok(shopDishService.listByShopId(shopId));
    }

    @PostMapping("/{shopId}/dishes")
    public ApiResponse<ShopDish> addDish(@PathVariable("shopId") Long shopId,
                                         @RequestHeader("X-Merchant-Id") Long merchantId,
                                         @Valid @RequestBody DishCreateRequest request) {
        Shop shop = shopService.getById(shopId);
        if (shop == null || !merchantId.equals(shop.getMerchantId())) {
            throw new BusinessException("无权访问此店铺");
        }
        ShopDish dish = new ShopDish();
        dish.setName(request.getName());
        dish.setPrice(request.getPrice());
        dish.setDescription(request.getDescription());
        dish.setImageUrl(request.getImageUrl());
        return ApiResponse.ok(shopDishService.addDish(shopId, merchantId, dish));
    }

    @PutMapping("/{shopId}/dishes/{dishId}")
    public ApiResponse<ShopDish> updateDish(@PathVariable("shopId") Long shopId,
                                            @PathVariable("dishId") Long dishId,
                                            @RequestHeader("X-Merchant-Id") Long merchantId,
                                            @RequestBody ShopDish updateData) {
        Shop shop = shopService.getById(shopId);
        if (shop == null || !merchantId.equals(shop.getMerchantId())) {
            throw new BusinessException("无权访问此店铺");
        }
        return ApiResponse.ok(shopDishService.updateDish(dishId, updateData));
    }

    @DeleteMapping("/{shopId}/dishes/{dishId}")
    public ApiResponse<Void> deleteDish(@PathVariable("shopId") Long shopId,
                                        @PathVariable("dishId") Long dishId,
                                        @RequestHeader("X-Merchant-Id") Long merchantId) {
        Shop shop = shopService.getById(shopId);
        if (shop == null || !merchantId.equals(shop.getMerchantId())) {
            throw new BusinessException("无权访问此店铺");
        }
        shopDishService.deleteDish(dishId);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{shopId}/dishes/{dishId}/status")
    public ApiResponse<Void> updateDishStatus(@PathVariable("shopId") Long shopId,
                                              @PathVariable("dishId") Long dishId,
                                              @RequestHeader("X-Merchant-Id") Long merchantId,
                                              @RequestParam Integer status) {
        Shop shop = shopService.getById(shopId);
        if (shop == null || !merchantId.equals(shop.getMerchantId())) {
            throw new BusinessException("无权访问此店铺");
        }
        shopDishService.updateStatus(dishId, status);
        return ApiResponse.ok(null);
    }
}
