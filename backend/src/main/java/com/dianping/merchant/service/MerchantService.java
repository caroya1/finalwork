package com.dianping.merchant.service;

import com.dianping.merchant.entity.Merchant;
import com.dianping.merchant.mapper.MerchantMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MerchantService {
    private final MerchantMapper merchantMapper;

    public MerchantService(MerchantMapper merchantMapper) {
        this.merchantMapper = merchantMapper;
    }

    public Merchant create(Merchant merchant) {
        merchant.touchForCreate();
        merchantMapper.insert(merchant);
        return merchant;
    }

    public List<Merchant> list() {
        return merchantMapper.selectList(null);
    }
}
