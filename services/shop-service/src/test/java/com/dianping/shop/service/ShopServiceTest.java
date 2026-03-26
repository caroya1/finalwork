package com.dianping.shop.service;

import com.dianping.common.dto.AuditRequest;
import com.dianping.common.dto.AuditResult;
import com.dianping.common.port.AiPort;
import com.dianping.shop.entity.Shop;
import com.dianping.shop.mapper.ShopMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopMapper shopMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private AiPort aiPort;

    private ShopService shopService;
    private Shop validShop;

    @BeforeEach
    void setUp() {
        shopService = new ShopService(shopMapper, redisTemplate, aiPort, 300);

        validShop = new Shop();
        validShop.setName("Test Shop");
        validShop.setCategory("Restaurant");
        validShop.setAddress("123 Test Street");
        validShop.setCity("Beijing");
        validShop.setTags("food, dining");
        validShop.setMerchantId(1L);
    }

    @Test
    void createShop_WithApprovedContent_ShouldCreateShop() {
        AuditResult approvedResult = new AuditResult();
        approvedResult.setApproved(true);
        approvedResult.setReason("Content is appropriate");
        approvedResult.setConfidence(0.95);
        approvedResult.setAuditType("SHOP");

        when(aiPort.audit(any())).thenReturn(approvedResult);
        when(shopMapper.insert(any(Shop.class))).thenAnswer(invocation -> {
            Shop shop = invocation.getArgument(0);
            shop.setId(1L);
            return 1;
        });

        Shop createdShop = shopService.create(validShop);

        assertNotNull(createdShop);
        assertEquals(1L, createdShop.getId());
        assertEquals(1, createdShop.getAuditStatus());
        assertNull(createdShop.getAuditRemark());
        verify(aiPort).audit(any());
        verify(shopMapper).insert(any(Shop.class));
    }

    @Test
    void createShop_WithRejectedContent_ShouldThrowException() {
        AuditResult rejectedResult = new AuditResult();
        rejectedResult.setApproved(false);
        rejectedResult.setReason("Content contains inappropriate language");
        rejectedResult.setConfidence(0.88);
        rejectedResult.setAuditType("SHOP");

        when(aiPort.audit(any())).thenReturn(rejectedResult);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> shopService.create(validShop)
        );

        assertTrue(exception.getMessage().contains("Shop application rejected"));
        assertTrue(exception.getMessage().contains("Content contains inappropriate language"));
        verify(aiPort).audit(any());
        verify(shopMapper, never()).insert(any(Shop.class));
    }

    @Test
    void createShop_WithRejectedContent_NoReason_ShouldUseDefaultMessage() {
        AuditResult rejectedResult = new AuditResult();
        rejectedResult.setApproved(false);
        rejectedResult.setReason(null);
        rejectedResult.setConfidence(0.88);
        rejectedResult.setAuditType("SHOP");

        when(aiPort.audit(any())).thenReturn(rejectedResult);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> shopService.create(validShop)
        );

        assertTrue(exception.getMessage().contains("Content violates community guidelines"));
        verify(aiPort).audit(any());
        verify(shopMapper, never()).insert(any(Shop.class));
    }

    @Test
    void createShop_AiServiceFailure_ShouldFallbackToAutoApprove() {
        when(aiPort.audit(any())).thenThrow(new RuntimeException("AI service unavailable"));
        when(shopMapper.insert(any(Shop.class))).thenAnswer(invocation -> {
            Shop shop = invocation.getArgument(0);
            shop.setId(1L);
            return 1;
        });

        Shop createdShop = shopService.create(validShop);

        assertNotNull(createdShop);
        assertEquals(1L, createdShop.getId());
        assertEquals(1, createdShop.getAuditStatus());
        verify(aiPort).audit(any());
        verify(shopMapper).insert(any(Shop.class));
    }

    @Test
    void createShop_WithEmptyContent_ShouldStillAudit() {
        Shop emptyShop = new Shop();
        emptyShop.setMerchantId(1L);

        AuditResult approvedResult = new AuditResult();
        approvedResult.setApproved(true);
        approvedResult.setReason("Auto-approved");
        approvedResult.setConfidence(1.0);
        approvedResult.setAuditType("SHOP");

        when(aiPort.audit(any())).thenReturn(approvedResult);
        when(shopMapper.insert(any(Shop.class))).thenAnswer(invocation -> {
            Shop shop = invocation.getArgument(0);
            shop.setId(1L);
            return 1;
        });

        Shop createdShop = shopService.create(emptyShop);

        assertNotNull(createdShop);
        assertEquals(1, createdShop.getAuditStatus());
        verify(aiPort).audit(any());
    }

    @Test
    void createShop_AuditRequestContainsAllFields() {
        AuditResult approvedResult = new AuditResult();
        approvedResult.setApproved(true);
        approvedResult.setConfidence(0.95);
        approvedResult.setAuditType("SHOP");

        when(aiPort.audit(any())).thenReturn(approvedResult);
        when(shopMapper.insert(any(Shop.class))).thenAnswer(invocation -> {
            Shop shop = invocation.getArgument(0);
            shop.setId(1L);
            return 1;
        });

        shopService.create(validShop);

        verify(aiPort).audit(argThat((AuditRequest request) ->
                request.getType().equals("SHOP") &&
                        request.getContent().contains("Test Shop") &&
                        request.getContent().contains("Restaurant") &&
                        request.getContent().contains("123 Test Street") &&
                        request.getContent().contains("food, dining")
        ));
    }
}
