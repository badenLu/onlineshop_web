package com.ecommerce.backend;

import com.ecommerce.backend.config.TestInfrastructureConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestInfrastructureConfig.class)
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
