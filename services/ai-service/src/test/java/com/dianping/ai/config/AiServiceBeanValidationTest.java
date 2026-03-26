package com.dianping.ai.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.nacos.config.enabled=false",
    "spring.cloud.nacos.discovery.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "ai.dashscope.api-key=test-key"
})
class AiServiceBeanValidationTest {

    @MockBean
    private MeterRegistry meterRegistry;

    @Test
    void testAllRequiredBeansExist(ApplicationContext context) {
        assertThat(context.containsBean("aiAuditService"))
            .as("AiAuditService bean should exist")
            .isTrue();
        
        assertThat(context.containsBean("dashScopeClient"))
            .as("DashScopeClient bean should exist")
            .isTrue();
        
        assertThat(context.containsBean("intentParser"))
            .as("IntentParser bean should exist")
            .isTrue();
        
        assertThat(context.containsBean("aiApiMetrics"))
            .as("AiApiMetrics bean should exist")
            .isTrue();
        
        System.out.println("All required beans are created successfully!");
    }

    @Test
    void testConfigurationPropertiesLoaded(ApplicationContext context) {
        AiConfig aiConfig = context.getBean(AiConfig.class);
        
        assertThat(aiConfig).isNotNull();
        assertThat(aiConfig.getBaseUrl()).isNotNull();
        assertThat(aiConfig.getModel()).isNotNull();
        
        System.out.println("Configuration properties loaded: " + aiConfig.getModel());
    }
}
