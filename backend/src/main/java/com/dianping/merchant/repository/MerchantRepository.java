package com.dianping.merchant.repository;

import com.dianping.merchant.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {
}
