package io.bmatch.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"spring.cloud.consul.discovery.register=false", "spring.cloud.consul.discovery.enabled=false"})
class ApplicationTests {

    @Test
    void contextLoads() {
    }

}
