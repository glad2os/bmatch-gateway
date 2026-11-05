package io.bmatch.gateway.controller;

import io.bmatch.gateway.dto.UpdateProfileRequest;
import io.bmatch.gateway.dto.UserProfile;
import io.bmatch.gateway.exceptions.UserAlreadyExistsException;
import io.bmatch.gateway.model.User;
import io.bmatch.gateway.services.JwtService;
import io.bmatch.gateway.services.RedisUserService;
import io.bmatch.gateway.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProfileControllerTest {
    private UserService service;
    private RedisUserService cache;
    private WebTestClient signedIn;
    private WebTestClient anonymous;

    @BeforeEach
    void setUp() {
        service = mock(UserService.class);
        cache = mock(RedisUserService.class);
        when(cache.evict(anyLong())).thenReturn(Mono.empty());
        UserController controller = new UserController(service, mock(JwtService.class), cache);
        anonymous = WebTestClient.bindToController(controller).build();
        signedIn = WebTestClient.bindToController(controller)
                .webFilter((exchange, chain) -> chain.filter(exchange.mutate()
                        .principal(Mono.just(new UsernamePasswordAuthenticationToken("7", null, List.of()))).build()))
                .build();
    }

    private static Map<String, Object> validProfile() {
        return Map.of("username", "new_name", "firstName", "Ann", "lastName", "Lee", "birthday", "1990-05-01",
                "links", List.of("https://github.com/ann"), "background", "ocean");
    }

    @Test
    void updatesProfileOfTheSignedInUserAndDropsCachedCopies() {
        User saved = new User();
        saved.setId(7L);
        saved.setUsername("new_name");
        saved.setFirstName("Ann");
        saved.setBirthday(LocalDate.of(1990, 5, 1));
        saved.setLinks("[\"https://github.com/ann\"]");
        saved.setBackground("ocean");
        when(service.updateProfile(eq("7"), any(UpdateProfileRequest.class))).thenReturn(Mono.just(saved));

        signedIn.put().uri("/api/user/profile").contentType(MediaType.APPLICATION_JSON).bodyValue(validProfile())
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.username").isEqualTo("new_name")
                .jsonPath("$.birthday").isEqualTo("1990-05-01")
                .jsonPath("$.links[0]").isEqualTo("https://github.com/ann")
                .jsonPath("$.background").isEqualTo("ocean");
        verify(cache).evict(7L);
    }

    @Test
    void rejectsInvalidProfileDataAndTakenUsernames() {
        for (Map<String, Object> body : List.of(
                Map.<String, Object>of("username", "a#b"),
                Map.<String, Object>of("username", "valid_name", "birthday", "2999-01-01"),
                Map.<String, Object>of("username", "valid_name", "links", List.of("javascript:alert(1)")),
                Map.<String, Object>of("username", "valid_name", "background", "neon"))) {
            signedIn.put().uri("/api/user/profile").contentType(MediaType.APPLICATION_JSON).bodyValue(body)
                    .exchange().expectStatus().isBadRequest();
        }
        verifyNoInteractions(service);

        when(service.updateProfile(eq("7"), any())).thenReturn(Mono.error(new UserAlreadyExistsException("Username already exists")));
        signedIn.put().uri("/api/user/profile").contentType(MediaType.APPLICATION_JSON).bodyValue(validProfile())
                .exchange().expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.message").isEqualTo("Username already exists");
        anonymous.put().uri("/api/user/profile").contentType(MediaType.APPLICATION_JSON).bodyValue(validProfile())
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    void changesPasswordOnlyWithTheCurrentOne() {
        when(service.changePassword("7", "old-pass", "new-pass")).thenReturn(Mono.just(new User()));
        signedIn.put().uri("/api/user/password").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("currentPassword", "old-pass", "newPassword", "new-pass"))
                .exchange().expectStatus().isOk();
        when(service.changePassword("7", "wrong", "new-pass"))
                .thenReturn(Mono.error(new BadCredentialsException("Current password is incorrect")));
        signedIn.put().uri("/api/user/password").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("currentPassword", "wrong", "newPassword", "new-pass"))
                .exchange().expectStatus().isBadRequest()
                .expectBody().jsonPath("$.message").isEqualTo("Current password is incorrect");
        signedIn.put().uri("/api/user/password").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("currentPassword", "old-pass", "newPassword", "short"))
                .exchange().expectStatus().isBadRequest();
    }

    @Test
    void publicProfileHidesPrivateFields() {
        UserProfile profile = new UserProfile();
        profile.setId(7L);
        profile.setUsername("ann");
        profile.setEmail("ann@example.com");
        profile.setBirthday("1990-05-01");
        profile.setMmr(2000);
        profile.setLinks(List.of("https://github.com/ann"));
        when(cache.findUserById("7")).thenReturn(Mono.just(profile));
        when(cache.findUserById("8")).thenReturn(Mono.error(new UsernameNotFoundException("User not found")));

        anonymous.get().uri("/api/user/public/7").exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.username").isEqualTo("ann")
                .jsonPath("$.links[0]").isEqualTo("https://github.com/ann")
                .jsonPath("$.email").doesNotExist()
                .jsonPath("$.birthday").doesNotExist();
        anonymous.get().uri("/api/user/public/8").exchange().expectStatus().isNotFound();
        anonymous.get().uri("/api/user/public/abc").exchange().expectStatus().isNotFound();
    }
}
