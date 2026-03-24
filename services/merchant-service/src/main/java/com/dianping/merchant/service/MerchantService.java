package com.dianping.merchant.service;

import com.dianping.common.exception.BusinessException;
import com.dianping.common.port.PasswordPort;
import com.dianping.merchant.dto.MerchantLoginRequest;
import com.dianping.merchant.dto.MerchantLoginResponse;
import com.dianping.merchant.dto.MerchantRegisterRequest;
import com.dianping.merchant.entity.Merchant;
import com.dianping.merchant.enums.MerchantStatus;
import com.dianping.merchant.mapper.MerchantMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MerchantService {
    private static final String MERCHANT_LIST_CACHE_KEY = "dp:merchant:list";
    private static final String MERCHANT_CACHE_PREFIX = "dp:merchant:";

    private final MerchantMapper merchantMapper;
    private final MerchantJwtService jwtService;
    private final PasswordPort passwordPort;
    private final RedisTemplate<String, Object> redisTemplate;
    private final long cacheTtlSeconds;

    public MerchantService(MerchantMapper merchantMapper,
                           MerchantJwtService jwtService,
                           PasswordPort passwordPort,
                           RedisTemplate<String, Object> redisTemplate,
                           @Value("${app.merchant.cache-ttl-seconds:300}") long cacheTtlSeconds) {
        this.merchantMapper = merchantMapper;
        this.jwtService = jwtService;
        this.passwordPort = passwordPort;
        this.redisTemplate = redisTemplate;
        this.cacheTtlSeconds = cacheTtlSeconds;
    }

    public MerchantLoginResponse register(MerchantRegisterRequest request) {
        Merchant existing = merchantMapper.findByEmail(request.getEmail());
        if (existing != null) {
            throw new BusinessException("该邮箱已被注册");
        }

        Merchant merchant = new Merchant();
        merchant.setName(request.getName());
        merchant.setContactName(request.getContactName());
        merchant.setContactPhone(request.getContactPhone());
        merchant.setEmail(request.getEmail());
        merchant.setPasswordHash(passwordPort.encode(request.getPassword()));
        merchant.setCategory(request.getCategory());
        merchant.setCity(request.getCity());
        merchant.setStatus(MerchantStatus.PENDING.getCode());
        merchant.touchForCreate();
        merchantMapper.insert(merchant);

        return buildLoginResponse(merchant);
    }

    public MerchantLoginResponse login(MerchantLoginRequest request) {
        Merchant merchant = merchantMapper.findByEmail(request.getEmail());
        if (merchant == null) {
            throw new BusinessException("邮箱或密码错误");
        }

        if (!passwordPort.matches(request.getPassword(), merchant.getPasswordHash())) {
            throw new BusinessException("邮箱或密码错误");
        }

        if (merchant.getStatus() == MerchantStatus.PENDING.getCode()) {
            throw new BusinessException("账号正在审核中，请耐心等待");
        }

        if (merchant.getStatus() == MerchantStatus.DISABLED.getCode()) {
            throw new BusinessException("账号已被禁用");
        }

        return buildLoginResponse(merchant);
    }

    private MerchantLoginResponse buildLoginResponse(Merchant merchant) {
        String token = jwtService.generateToken(merchant.getId(), merchant.getEmail());
        MerchantLoginResponse response = new MerchantLoginResponse();
        response.setMerchantId(merchant.getId());
        response.setName(merchant.getName());
        response.setEmail(merchant.getEmail());
        response.setToken(token);
        return response;
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
            @SuppressWarnings("unchecked")
            List<Merchant> result = (List<Merchant>) cached;
            return result;
        }
        List<Merchant> merchants = merchantMapper.selectList(null);
        if (!merchants.isEmpty()) {
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
            throw new BusinessException("商户不存在");
        }
        applyUpdate(merchant, request);
        merchant.touchForUpdate();
        merchantMapper.updateById(merchant);
        invalidateCache(id);
        return merchant;
    }

    public Merchant updateStatus(Long id, Integer status) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException("商户不存在");
        }
        merchant.setStatus(status);
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
        if (StringUtils.hasText(request.getContactName())) {
            merchant.setContactName(request.getContactName().trim());
        }
        if (StringUtils.hasText(request.getContactPhone())) {
            merchant.setContactPhone(request.getContactPhone().trim());
        }
    }

    private void invalidateCache(Long merchantId) {
        redisTemplate.delete(MERCHANT_LIST_CACHE_KEY);
        if (merchantId != null) {
            redisTemplate.delete(MERCHANT_CACHE_PREFIX + merchantId);
        }
    }
}
