package com.dianping.ai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.nacos.config.enabled=false",
    "spring.cloud.nacos.discovery.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "ai.dashscope.api-key=test-key"
})
class AiServiceApplicationTest {

    @Test
    void contextLoads() {
        System.out.println("✅ AI Service context loaded successfully!");
    }

    @Test
    void testServiceStartup() {
        System.out.println("✅ AI Service can start without errors!");
    }
}
