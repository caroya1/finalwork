package com.dianping.merchant.service;

import com.dianping.common.dto.AuditRequest;
import com.dianping.common.dto.AuditResult;
import com.dianping.common.exception.BusinessException;
import com.dianping.common.port.AiPort;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class MerchantService {
    private static final String ACCESS_TOKEN_PREFIX = "dp:merchant:token:access:";
    private static final String REFRESH_TOKEN_PREFIX = "dp:merchant:token:refresh:";
    private static final String MERCHANT_CACHE_PREFIX = "dp:merchant:";
    private static final String MERCHANT_LIST_CACHE_KEY = "dp:merchant:list";

    private final MerchantMapper merchantMapper;
    private final MerchantJwtService jwtService;
    private final PasswordPort passwordPort;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AiPort aiPort;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    private final long cacheTtlSeconds;

    public MerchantService(MerchantMapper merchantMapper,
                           MerchantJwtService jwtService,
                           PasswordPort passwordPort,
                           RedisTemplate<String, Object> redisTemplate,
                           AiPort aiPort,
                           @Value("${app.jwt.expire-minutes:120}") long accessExpireMinutes,
                           @Value("${app.jwt.refresh-expire-days:7}") long refreshExpireDays) {
        this.merchantMapper = merchantMapper;
        this.jwtService = jwtService;
        this.passwordPort = passwordPort;
        this.redisTemplate = redisTemplate;
        this.aiPort = aiPort;
        this.accessTtlSeconds = accessExpireMinutes * 60;
        this.refreshTtlSeconds = refreshExpireDays * 24 * 60 * 60;
        this.cacheTtlSeconds = 300;
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

        revokeExistingTokens(merchant.getId());
        return buildLoginResponse(merchant);
    }

    private void revokeExistingTokens(Long merchantId) {
        String accessPattern = ACCESS_TOKEN_PREFIX + merchantId + ":*";
        String refreshPattern = REFRESH_TOKEN_PREFIX + merchantId + ":*";
        
        var accessKeys = redisTemplate.keys(accessPattern);
        var refreshKeys = redisTemplate.keys(refreshPattern);
        
        if (accessKeys != null && !accessKeys.isEmpty()) {
            redisTemplate.delete(accessKeys);
        }
        if (refreshKeys != null && !refreshKeys.isEmpty()) {
            redisTemplate.delete(refreshKeys);
        }
    }

    private MerchantLoginResponse buildLoginResponse(Merchant merchant) {
        String accessToken = jwtService.generateAccessToken(merchant.getId(), merchant.getEmail());
        String refreshToken = jwtService.generateRefreshToken(merchant.getId(), merchant.getEmail());
        
        Map<String, String> payload = new HashMap<>();
        payload.put("merchantId", String.valueOf(merchant.getId()));
        payload.put("email", merchant.getEmail());
        payload.put("name", merchant.getName());
        payload.put("role", "merchant");
        
        redisTemplate.opsForValue().set(ACCESS_TOKEN_PREFIX + merchant.getId() + ":" + accessToken, payload, accessTtlSeconds, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + merchant.getId() + ":" + refreshToken, payload, refreshTtlSeconds, TimeUnit.SECONDS);
        
        MerchantLoginResponse response = new MerchantLoginResponse();
        response.setMerchantId(merchant.getId());
        response.setName(merchant.getName());
        response.setEmail(merchant.getEmail());
        response.setToken(accessToken);
        response.setRefreshToken(refreshToken);
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

    public Merchant approveWithAiAudit(Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException("商户不存在");
        }

        if (merchant.getStatus() != MerchantStatus.PENDING.getCode()) {
            throw new BusinessException("只有待审核的商户才能进行AI审核");
        }

        String content = buildAuditContent(merchant);
        AuditRequest auditRequest = new AuditRequest(content, "MERCHANT", merchant.getId());

        AuditResult auditResult;
        try {
            auditResult = aiPort.audit(auditRequest);
        } catch (Exception e) {
            throw new BusinessException("AI审核服务调用失败: " + e.getMessage());
        }

        if (auditResult == null || auditResult.getApproved() == null) {
            throw new BusinessException("AI审核结果无效");
        }

        LocalDateTime now = LocalDateTime.now();
        merchant.setAiAuditTime(now);

        if (Boolean.TRUE.equals(auditResult.getApproved())) {
            merchant.setStatus(MerchantStatus.NORMAL.getCode());
            merchant.setAiAuditStatus(1);
            merchant.setAiAuditReason(null);
        } else {
            merchant.setStatus(MerchantStatus.DISABLED.getCode());
            merchant.setAiAuditStatus(0);
            merchant.setAiAuditReason(auditResult.getReason());
        }

        merchant.touchForUpdate();
        merchantMapper.updateById(merchant);
        invalidateCache(id);

        return merchant;
    }

    private String buildAuditContent(Merchant merchant) {
        StringBuilder content = new StringBuilder();
        content.append("商户名称: ").append(merchant.getName()).append("\n");
        content.append("经营类目: ").append(merchant.getCategory()).append("\n");
        content.append("所在城市: ").append(merchant.getCity()).append("\n");
        content.append("联系人: ").append(merchant.getContactName()).append("\n");
        content.append("联系电话: ").append(merchant.getContactPhone()).append("\n");
        content.append("联系邮箱: ").append(merchant.getEmail());
        return content.toString();
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
