package com.dianping.merchant.service;

import com.dianping.merchant.entity.Merchant;
import com.dianping.merchant.repository.MerchantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MerchantService {
    private final MerchantRepository merchantRepository;

    public MerchantService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    public Merchant create(Merchant merchant) {
        return merchantRepository.save(merchant);
    }

    public List<Merchant> list() {
        return merchantRepository.findAll();
    }
}
