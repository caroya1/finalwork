package com.dianping.merchant.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.merchant.entity.Merchant;
import com.dianping.merchant.service.MerchantService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/merchants")
@Validated
public class MerchantController {
    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
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
            return ApiResponse.fail("merchant not found");
        }
        return ApiResponse.ok(merchant);
    }

    @PutMapping("/{id}")
    public ApiResponse<Merchant> update(@PathVariable("id") Long id,
                                        @RequestBody Merchant merchant) {
        return ApiResponse.ok(merchantService.update(id, merchant));
    }
}
