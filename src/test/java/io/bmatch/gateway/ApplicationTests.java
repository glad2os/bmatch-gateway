package io.bmatch.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;

// A real server: CORS preflights are checked the way a browser sends them.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.cloud.consul.discovery.register=false", "spring.cloud.consul.discovery.enabled=false",
        "spring.sql.init.mode=never"})
class ApplicationTests {

    @Autowired
    private WebTestClient client;

    @Test
    void contextLoads() {
    }

    @Test
    void corsPreflightsPassSecurityForAuthenticatedEndpoints() {
        for (String path : new String[]{"/api/user/profile", "/api/user/password"}) {
            client.options().uri(path)
                    .header("Origin", "http://localhost:5173")
                    .header("Access-Control-Request-Method", HttpMethod.PUT.name())
                    .header("Access-Control-Request-Headers", "authorization,content-type")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:5173");
        }
    }

    @Test
    void editingTheProfileRequiresSigningIn() {
        client.put().uri("/api/user/profile").bodyValue("{}").exchange().expectStatus().isUnauthorized();
    }
}
