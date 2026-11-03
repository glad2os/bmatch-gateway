package io.bmatch.gateway.controller;

import io.bmatch.gateway.exceptions.UserAlreadyExistsException;
import io.bmatch.gateway.model.User;
import io.bmatch.gateway.services.JwtService;
import io.bmatch.gateway.services.RedisUserService;
import io.bmatch.gateway.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import java.util.Map;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserControllerTest {
    private UserService service;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        service = mock(UserService.class);
        client = WebTestClient.bindToController(new UserController(service,
                mock(JwtService.class), mock(RedisUserService.class))).build();
    }

    @Test
    void validatesReactiveRequestBodyAndReturnsApiResponse() {
        for (Map<String, String> body : java.util.List.of(
                Map.of("username", "ab", "email", "test@example.com", "password", "abcdef"),
                Map.of("username", "bad#name", "email", "test@example.com", "password", "abcdef"),
                Map.of("username", "bad/name", "email", "test@example.com", "password", "abcdef"),
                Map.of("username", "valid_name", "email", "invalid", "password", "abcdef"),
                Map.of("username", "valid_name", "email", "test@example.com", "password", "short"),
                Map.of("username", "valid_name", "email", "test@example.com", "password", "x".repeat(73)),
                Map.of("username", "valid_name", "email", "test@example.com"))) {
            client.post().uri("/api/user/create").contentType(MediaType.APPLICATION_JSON).bodyValue(body)
                    .exchange().expectStatus().isBadRequest()
                    .expectBody().jsonPath("$.message").isEqualTo("Invalid registration data")
                    .jsonPath("$.statusCode").isEqualTo(400);
        }
        verifyNoInteractions(service);
    }

    @Test
    void returnsSuccessOrConflict() {
        Map<String, String> body = Map.of("username", "valid_name", "email", "test@example.com", "password", "abcdef");
        when(service.createUser(anyString(), anyString(), anyString())).thenReturn(Mono.just(new User()));
        client.post().uri("/api/user/create").bodyValue(body).exchange().expectStatus().isOk();
        when(service.createUser(anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new UserAlreadyExistsException("Already exists")));
        client.post().uri("/api/user/create").bodyValue(body).exchange().expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.message").isEqualTo("Already exists").jsonPath("$.statusCode").isEqualTo(409);
    }
}
