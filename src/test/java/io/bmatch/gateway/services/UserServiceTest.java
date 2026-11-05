package io.bmatch.gateway.services;

import io.bmatch.gateway.exceptions.UserAlreadyExistsException;
import io.bmatch.gateway.model.User;
import io.bmatch.gateway.repository.UserRepository;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.Set;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private UserRepository repository;
    private UserService service;
    private PasswordEncoder encoder;
    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        encoder = mock(PasswordEncoder.class);
        Validator validator = mock(Validator.class);
        when(validator.validate(any(User.class))).thenReturn(Set.of());
        when(encoder.encode(anyString())).thenReturn("hash");
        when(repository.findByEmail(anyString())).thenReturn(Mono.empty());
        when(repository.findByUsername(anyString())).thenReturn(Mono.empty());
        service = new UserService(repository, encoder, validator);
    }
    @Test
    void checksUsernameAndEmailAndMapsDatabaseRace() {
        when(repository.findByUsername("taken")).thenReturn(Mono.just(new User()));
        StepVerifier.create(service.createUser("taken", "abcdef", "test@example.com"))
                .expectError(UserAlreadyExistsException.class).verify();
        when(repository.findByEmail("taken@example.com")).thenReturn(Mono.just(new User()));
        StepVerifier.create(service.createUser("free", "abcdef", "taken@example.com"))
                .expectError(UserAlreadyExistsException.class).verify();
        verify(repository, never()).save(any());
        when(repository.save(any(User.class))).thenReturn(Mono.error(new DuplicateKeyException("unique constraint")));
        StepVerifier.create(service.createUser("free", "abcdef", "test@example.com"))
                .expectError(UserAlreadyExistsException.class).verify();
    }
    @Test
    void refusesDisabledAndLockedAccounts() {
        User user = new User();
        user.setPassword("hash");
        when(repository.findByEmail(anyString())).thenReturn(Mono.just(user));
        when(encoder.matches("abcdef", "hash")).thenReturn(true);
        user.setAccountNonLocked(true);
        StepVerifier.create(service.validateUserByEmailAndPassword("test@example.com", "abcdef"))
                .expectError(BadCredentialsException.class).verify();
        user.setEnabled(true);
        user.setAccountNonLocked(false);
        StepVerifier.create(service.validateUserByEmailAndPassword("test@example.com", "abcdef"))
                .expectError(BadCredentialsException.class).verify();
        user.setAccountNonLocked(true);
        StepVerifier.create(service.validateUserByEmailAndPassword("test@example.com", "abcdef"))
                .expectNext(user).verifyComplete();
    }

    @Test
    void updatesProfileAndRefusesAUsernameOfAnotherUser() {
        User me = new User();
        me.setId(7L);
        me.setUsername("me");
        User other = new User();
        other.setId(8L);
        when(repository.findById(7L)).thenReturn(Mono.just(me));
        when(repository.findByUsername("other")).thenReturn(Mono.just(other));
        when(repository.save(any(User.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        io.bmatch.gateway.dto.UpdateProfileRequest request = new io.bmatch.gateway.dto.UpdateProfileRequest();
        request.setUsername("other");
        StepVerifier.create(service.updateProfile("7", request)).expectError(UserAlreadyExistsException.class).verify();

        request.setUsername("me");
        request.setFirstName("  Ann ");
        request.setLastName(" ");
        request.setLinks(java.util.List.of("https://github.com/ann"));
        request.setBackground("ocean");
        StepVerifier.create(service.updateProfile("7", request)).assertNext(saved -> {
            org.junit.jupiter.api.Assertions.assertEquals("Ann", saved.getFirstName());
            org.junit.jupiter.api.Assertions.assertNull(saved.getLastName());
            org.junit.jupiter.api.Assertions.assertEquals("[\"https://github.com/ann\"]", saved.getLinks());
            org.junit.jupiter.api.Assertions.assertEquals("ocean", saved.getBackground());
        }).verifyComplete();
    }

    @Test
    void changesPasswordOnlyWhenTheCurrentOneMatches() {
        User me = new User();
        me.setId(7L);
        me.setPassword("hash");
        when(repository.findById(7L)).thenReturn(Mono.just(me));
        when(repository.save(any(User.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(encoder.matches("wrong", "hash")).thenReturn(false);
        when(encoder.matches("right", "hash")).thenReturn(true);
        when(encoder.encode("new-pass")).thenReturn("new-hash");

        StepVerifier.create(service.changePassword("7", "wrong", "new-pass")).expectError(BadCredentialsException.class).verify();
        StepVerifier.create(service.changePassword("7", "right", "new-pass"))
                .assertNext(saved -> org.junit.jupiter.api.Assertions.assertEquals("new-hash", saved.getPassword()))
                .verifyComplete();
    }
}
