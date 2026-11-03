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
}
