package com.dianping.merchant.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.common.exception.BusinessException;
import com.dianping.merchant.dto.MerchantLoginRequest;
import com.dianping.merchant.dto.MerchantLoginResponse;
import com.dianping.merchant.dto.MerchantRegisterRequest;
import com.dianping.merchant.entity.Merchant;
import com.dianping.merchant.service.MerchantService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/merchants")
@Validated
public class MerchantController {
    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping("/register")
    public ApiResponse<MerchantLoginResponse> register(@Valid @RequestBody MerchantRegisterRequest request) {
        return ApiResponse.ok(merchantService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<MerchantLoginResponse> login(@Valid @RequestBody MerchantLoginRequest request) {
        return ApiResponse.ok(merchantService.login(request));
    }

    @PostMapping
    public ApiResponse<Merchant> create(@Valid @RequestBody Merchant merchant) {
        return ApiResponse.ok(merchantService.create(merchant));
    }

    @GetMapping
    public ApiResponse<List<Merchant>> list() {
        return ApiResponse.ok(merchantService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<Merchant> detail(@PathVariable("id") Long id) {
        Merchant merchant = merchantService.getById(id);
        if (merchant == null) {
            throw new BusinessException("商户不存在");
        }
        return ApiResponse.ok(merchant);
    }

    @PutMapping("/{id}")
    public ApiResponse<Merchant> update(@PathVariable("id") Long id, @RequestBody Merchant merchant) {
        return ApiResponse.ok(merchantService.update(id, merchant));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Merchant> updateStatus(@PathVariable("id") Long id, @RequestParam Integer status) {
        return ApiResponse.ok(merchantService.updateStatus(id, status));
    }
}
