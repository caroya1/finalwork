package com.dianping.shop.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.common.exception.BusinessException;
import com.dianping.shop.entity.Shop;
import com.dianping.shop.service.ShopService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.shop.mapper.ShopMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shops")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminShopController {
    private final ShopService shopService;
    private final ShopMapper shopMapper;

    public AdminShopController(ShopService shopService, ShopMapper shopMapper) {
        this.shopService = shopService;
        this.shopMapper = shopMapper;
    }

    @GetMapping
    public ApiResponse<List<Shop>> list(@RequestParam(required = false) Integer auditStatus) {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        if (auditStatus != null) {
            wrapper.eq(Shop::getAuditStatus, auditStatus);
        }
        wrapper.orderByDesc(Shop::getCreatedAt);
        return ApiResponse.ok(shopMapper.selectList(wrapper));
    }

    @GetMapping("/{id}")
    public ApiResponse<Shop> get(@PathVariable("id") Long id) {
        Shop shop = shopService.getById(id);
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }
        return ApiResponse.ok(shop);
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<Shop> approve(@PathVariable("id") Long id) {
        Shop shop = shopMapper.selectById(id);
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }
        shop.setAuditStatus(1);
        shop.touchForUpdate();
        shopMapper.updateById(shop);
        return ApiResponse.ok(shop);
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<Shop> reject(@PathVariable("id") Long id, @RequestParam(required = false) String reason) {
        Shop shop = shopMapper.selectById(id);
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }
        shop.setAuditStatus(2);
        shop.setAuditRemark(reason);
        shop.touchForUpdate();
        shopMapper.updateById(shop);
        return ApiResponse.ok(shop);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Shop> updateStatus(@PathVariable("id") Long id, @RequestParam Integer status) {
        Shop shop = shopMapper.selectById(id);
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }
        shop.setStatus(status);
        shop.touchForUpdate();
        shopMapper.updateById(shop);
        return ApiResponse.ok(shop);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        shopService.delete(id);
        return ApiResponse.ok(null);
    }
}
