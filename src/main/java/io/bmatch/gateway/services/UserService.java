package io.bmatch.gateway.services;

import io.bmatch.gateway.exceptions.UserAlreadyExistsException;
import io.bmatch.gateway.model.User;
import io.bmatch.gateway.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.bmatch.gateway.enums.PermissionLevel;
import io.bmatch.gateway.enums.Status;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final Validator validator;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, Validator validator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
    }

    public Mono<User> createUser(String username, String password, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(PermissionLevel.PLAYER);
        user.setStatus(Status.REGISTERED);
        user.setMmr(2000);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        return userRepository.findByEmail(email).hasElement().flatMap(exists -> {
            if (exists) {
                return Mono.error(new UserAlreadyExistsException("User with email " + email + " already exists"));
            }

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            if (!violations.isEmpty()) {
                String errorMessage = violations.stream().map(violation -> violation.getMessage() + "; ").collect(Collectors.joining());
                return Mono.error(new ValidationException("Validation failed: " + errorMessage));
            }

            return userRepository.save(user);
        });
    }

    public Mono<UserDetails> validateUserByEmailAndPassword(String email, String password) throws BadCredentialsException {
        return userRepository.findByEmail(email).switchIfEmpty(Mono.error(new BadCredentialsException("Invalid credentials"))).filter(user -> passwordEncoder.matches(password, user.getPassword())).switchIfEmpty(Mono.error(new BadCredentialsException("Invalid credentials")));
    }

    public Mono<User> findUserById(String userId) {
        return userRepository.findById(Long.valueOf(userId)).switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")));
    }


}
