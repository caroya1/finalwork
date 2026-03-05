package com.dianping.merchant.service;

import com.dianping.common.exception.BusinessException;
import com.dianping.merchant.entity.Merchant;
import com.dianping.merchant.mapper.MerchantMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MerchantService {
    private static final String MERCHANT_LIST_CACHE_KEY = "dp:merchant:list";
    private static final String MERCHANT_CACHE_PREFIX = "dp:merchant:";

    private final MerchantMapper merchantMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final long cacheTtlSeconds;

    public MerchantService(MerchantMapper merchantMapper,
                           RedisTemplate<String, Object> redisTemplate,
                           @Value("${app.merchant.cache-ttl-seconds:300}") long cacheTtlSeconds) {
        this.merchantMapper = merchantMapper;
        this.redisTemplate = redisTemplate;
        this.cacheTtlSeconds = cacheTtlSeconds;
    }

    public Merchant create(Merchant merchant) {
        merchant.touchForCreate();
        merchantMapper.insert(merchant);
        invalidateCache(merchant.getId());
        return merchant;
    }

    public List<Merchant> list() {
        Object cached = redisTemplate.opsForValue().get(MERCHANT_LIST_CACHE_KEY);
        if (cached instanceof List) {
            return castList(cached);
        }
        List<Merchant> merchants = merchantMapper.selectList(null);
        if (!CollectionUtils.isEmpty(merchants)) {
            redisTemplate.opsForValue().set(MERCHANT_LIST_CACHE_KEY, merchants, cacheTtlSeconds, TimeUnit.SECONDS);
        }
        return merchants;
    }

    public Merchant getById(Long id) {
        if (id == null) {
            return null;
        }
        String cacheKey = MERCHANT_CACHE_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof Merchant) {
            return (Merchant) cached;
        }
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant != null) {
            redisTemplate.opsForValue().set(cacheKey, merchant, cacheTtlSeconds, TimeUnit.SECONDS);
        }
        return merchant;
    }

    public Merchant update(Long id, Merchant request) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException("merchant not found");
        }
        applyUpdate(merchant, request);
        merchant.touchForUpdate();
        merchantMapper.updateById(merchant);
        invalidateCache(id);
        return merchant;
    }

    private void applyUpdate(Merchant merchant, Merchant request) {
        if (request == null) {
            return;
        }
        if (StringUtils.hasText(request.getName())) {
            merchant.setName(request.getName().trim());
        }
        if (StringUtils.hasText(request.getCategory())) {
            merchant.setCategory(request.getCategory().trim());
        }
        if (StringUtils.hasText(request.getCity())) {
            merchant.setCity(request.getCity().trim());
        }
        if (StringUtils.hasText(request.getStatus())) {
            merchant.setStatus(request.getStatus().trim());
        }
        if (request.getRating() != null) {
            merchant.setRating(request.getRating());
        }
    }

    private void invalidateCache(Long merchantId) {
        redisTemplate.delete(MERCHANT_LIST_CACHE_KEY);
        if (merchantId != null) {
            redisTemplate.delete(MERCHANT_CACHE_PREFIX + merchantId);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Merchant> castList(Object cached) {
        if (cached instanceof List) {
            return (List<Merchant>) cached;
        }
        return new ArrayList<>();
    }
}
