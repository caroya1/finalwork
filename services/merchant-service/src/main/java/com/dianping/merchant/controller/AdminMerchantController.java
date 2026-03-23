package com.dianping.merchant.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.common.exception.BusinessException;
import com.dianping.merchant.entity.Merchant;
import com.dianping.merchant.enums.MerchantStatus;
import com.dianping.merchant.service.MerchantService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/merchants")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminMerchantController {
    private final MerchantService merchantService;

    public AdminMerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @GetMapping
    public ApiResponse<List<Merchant>> list() {
        return ApiResponse.ok(merchantService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<Merchant> get(@PathVariable("id") Long id) {
        Merchant merchant = merchantService.getById(id);
        if (merchant == null) {
            throw new BusinessException("商户不存在");
        }
        return ApiResponse.ok(merchant);
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<Merchant> approve(@PathVariable("id") Long id) {
        return ApiResponse.ok(merchantService.updateStatus(id, MerchantStatus.NORMAL.getCode()));
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<Merchant> reject(@PathVariable("id") Long id) {
        return ApiResponse.ok(merchantService.updateStatus(id, MerchantStatus.DISABLED.getCode()));
    }

    @PutMapping("/{id}/disable")
    public ApiResponse<Merchant> disable(@PathVariable("id") Long id) {
        return ApiResponse.ok(merchantService.updateStatus(id, MerchantStatus.DISABLED.getCode()));
    }

    @PutMapping("/{id}/enable")
    public ApiResponse<Merchant> enable(@PathVariable("id") Long id) {
        return ApiResponse.ok(merchantService.updateStatus(id, MerchantStatus.NORMAL.getCode()));
    }
}
