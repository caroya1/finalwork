package com.dianping.merchant.service;

import com.dianping.common.dto.AuditRequest;
import com.dianping.common.dto.AuditResult;
import com.dianping.common.exception.BusinessException;
import com.dianping.common.port.AiPort;
import com.dianping.common.port.PasswordPort;
import com.dianping.merchant.entity.Merchant;
import com.dianping.merchant.enums.MerchantStatus;
import com.dianping.merchant.mapper.MerchantMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MerchantServiceTest {

    @Mock
    private MerchantMapper merchantMapper;

    @Mock
    private MerchantJwtService jwtService;

    @Mock
    private PasswordPort passwordPort;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private AiPort aiPort;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private MerchantService merchantService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.keys(anyString())).thenReturn(java.util.Collections.emptySet());
        
        merchantService = new MerchantService(
            merchantMapper,
            jwtService,
            passwordPort,
            redisTemplate,
            aiPort,
            120,
            7
        );
    }

    @Test
    void approveWithAiAudit_Success_Approved() {
        // Given
        Long merchantId = 1L;
        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setName("Test Merchant");
        merchant.setCategory("Restaurant");
        merchant.setCity("Beijing");
        merchant.setContactName("John Doe");
        merchant.setContactPhone("1234567890");
        merchant.setEmail("test@example.com");
        merchant.setStatus(MerchantStatus.PENDING.getCode());

        AuditResult auditResult = new AuditResult();
        auditResult.setApproved(true);
        auditResult.setReason(null);
        auditResult.setConfidence(0.95);
        auditResult.setAuditType("MERCHANT");

        when(merchantMapper.selectById(merchantId)).thenReturn(merchant);
        when(aiPort.audit(any(AuditRequest.class))).thenReturn(auditResult);
        when(merchantMapper.updateById(Mockito.<Merchant>any())).thenReturn(1);

        // When
        Merchant result = merchantService.approveWithAiAudit(merchantId);

        // Then
        assertNotNull(result);
        assertEquals(MerchantStatus.NORMAL.getCode(), result.getStatus());
        assertEquals(1, result.getAiAuditStatus());
        assertNull(result.getAiAuditReason());
        assertNotNull(result.getAiAuditTime());

        verify(merchantMapper).selectById(merchantId);
        verify(aiPort).audit(any(AuditRequest.class));
        verify(merchantMapper).updateById(Mockito.<Merchant>any());
    }

    @Test
    void approveWithAiAudit_Success_Rejected() {
        // Given
        Long merchantId = 1L;
        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setName("Test Merchant");
        merchant.setCategory("Restaurant");
        merchant.setCity("Beijing");
        merchant.setContactName("John Doe");
        merchant.setContactPhone("1234567890");
        merchant.setEmail("test@example.com");
        merchant.setStatus(MerchantStatus.PENDING.getCode());

        AuditResult auditResult = new AuditResult();
        auditResult.setApproved(false);
        auditResult.setReason("Content contains sensitive information");
        auditResult.setConfidence(0.88);
        auditResult.setAuditType("MERCHANT");

        when(merchantMapper.selectById(merchantId)).thenReturn(merchant);
        when(aiPort.audit(any(AuditRequest.class))).thenReturn(auditResult);
        when(merchantMapper.updateById(Mockito.<Merchant>any())).thenReturn(1);

        // When
        Merchant result = merchantService.approveWithAiAudit(merchantId);

        // Then
        assertNotNull(result);
        assertEquals(MerchantStatus.DISABLED.getCode(), result.getStatus());
        assertEquals(0, result.getAiAuditStatus());
        assertEquals("Content contains sensitive information", result.getAiAuditReason());
        assertNotNull(result.getAiAuditTime());

        verify(merchantMapper).selectById(merchantId);
        verify(aiPort).audit(any(AuditRequest.class));
        verify(merchantMapper).updateById(Mockito.<Merchant>any());
    }

    @Test
    void approveWithAiAudit_MerchantNotFound() {
        // Given
        Long merchantId = 1L;
        when(merchantMapper.selectById(merchantId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            merchantService.approveWithAiAudit(merchantId);
        });
        assertEquals("商户不存在", exception.getMessage());

        verify(merchantMapper).selectById(merchantId);
        verify(aiPort, never()).audit(any());
    }

    @Test
    void approveWithAiAudit_NotPendingStatus() {
        // Given
        Long merchantId = 1L;
        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setStatus(MerchantStatus.NORMAL.getCode());

        when(merchantMapper.selectById(merchantId)).thenReturn(merchant);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            merchantService.approveWithAiAudit(merchantId);
        });
        assertEquals("只有待审核的商户才能进行AI审核", exception.getMessage());

        verify(merchantMapper).selectById(merchantId);
        verify(aiPort, never()).audit(any());
    }

    @Test
    void approveWithAiAudit_AiServiceFailure() {
        // Given
        Long merchantId = 1L;
        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setName("Test Merchant");
        merchant.setCategory("Restaurant");
        merchant.setCity("Beijing");
        merchant.setContactName("John Doe");
        merchant.setContactPhone("1234567890");
        merchant.setEmail("test@example.com");
        merchant.setStatus(MerchantStatus.PENDING.getCode());

        when(merchantMapper.selectById(merchantId)).thenReturn(merchant);
        when(aiPort.audit(any(AuditRequest.class))).thenThrow(new RuntimeException("AI service unavailable"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            merchantService.approveWithAiAudit(merchantId);
        });
        assertTrue(exception.getMessage().contains("AI审核服务调用失败"));

        verify(merchantMapper).selectById(merchantId);
        verify(aiPort).audit(any(AuditRequest.class));
        verify(merchantMapper, never()).updateById(Mockito.<Merchant>any());
    }

    @Test
    void approveWithAiAudit_NullAuditResult() {
        // Given
        Long merchantId = 1L;
        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setName("Test Merchant");
        merchant.setCategory("Restaurant");
        merchant.setCity("Beijing");
        merchant.setContactName("John Doe");
        merchant.setContactPhone("1234567890");
        merchant.setEmail("test@example.com");
        merchant.setStatus(MerchantStatus.PENDING.getCode());

        when(merchantMapper.selectById(merchantId)).thenReturn(merchant);
        when(aiPort.audit(any(AuditRequest.class))).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            merchantService.approveWithAiAudit(merchantId);
        });
        assertEquals("AI审核结果无效", exception.getMessage());

        verify(merchantMapper).selectById(merchantId);
        verify(aiPort).audit(any(AuditRequest.class));
        verify(merchantMapper, never()).updateById(Mockito.<Merchant>any());
    }

    @Test
    void approveWithAiAudit_NullApprovedFlag() {
        // Given
        Long merchantId = 1L;
        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setName("Test Merchant");
        merchant.setCategory("Restaurant");
        merchant.setCity("Beijing");
        merchant.setContactName("John Doe");
        merchant.setContactPhone("1234567890");
        merchant.setEmail("test@example.com");
        merchant.setStatus(MerchantStatus.PENDING.getCode());

        AuditResult auditResult = new AuditResult();
        auditResult.setApproved(null);
        auditResult.setReason("Unknown");

        when(merchantMapper.selectById(merchantId)).thenReturn(merchant);
        when(aiPort.audit(any(AuditRequest.class))).thenReturn(auditResult);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            merchantService.approveWithAiAudit(merchantId);
        });
        assertEquals("AI审核结果无效", exception.getMessage());

        verify(merchantMapper).selectById(merchantId);
        verify(aiPort).audit(any(AuditRequest.class));
        verify(merchantMapper, never()).updateById(Mockito.<Merchant>any());
    }

    @Test
    void approveWithAiAudit_ContentIncludesAllFields() {
        // Given
        Long merchantId = 1L;
        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setName("Test Merchant");
        merchant.setCategory("Restaurant");
        merchant.setCity("Beijing");
        merchant.setContactName("John Doe");
        merchant.setContactPhone("1234567890");
        merchant.setEmail("test@example.com");
        merchant.setStatus(MerchantStatus.PENDING.getCode());

        AuditResult auditResult = new AuditResult();
        auditResult.setApproved(true);
        auditResult.setConfidence(0.95);

        when(merchantMapper.selectById(merchantId)).thenReturn(merchant);
        when(aiPort.audit(argThat(request -> {
            return request.getType().equals("MERCHANT") &&
                   request.getTargetId().equals(merchantId) &&
                   request.getContent().contains("Test Merchant") &&
                   request.getContent().contains("Restaurant") &&
                   request.getContent().contains("Beijing") &&
                   request.getContent().contains("John Doe") &&
                   request.getContent().contains("1234567890") &&
                   request.getContent().contains("test@example.com");
        }))).thenReturn(auditResult);
        when(merchantMapper.updateById(Mockito.<Merchant>any())).thenReturn(1);

        // When
        Merchant result = merchantService.approveWithAiAudit(merchantId);

        // Then
        assertNotNull(result);
        verify(aiPort).audit(any(AuditRequest.class));
    }
}
