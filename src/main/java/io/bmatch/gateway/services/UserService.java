package io.bmatch.gateway.services;

import io.bmatch.gateway.dto.UpdateProfileRequest;
import io.bmatch.gateway.exceptions.UserAlreadyExistsException;
import io.bmatch.gateway.helpers.profile.ProfileLinks;
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
import org.springframework.dao.DuplicateKeyException;
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

        return Mono.zip(userRepository.findByEmail(email).hasElement(),
                        userRepository.findByUsername(username).hasElement()).flatMap(exists -> {
            if (exists.getT1() || exists.getT2()) {
                return Mono.error(new UserAlreadyExistsException("Email or username already exists"));
            }

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            if (!violations.isEmpty()) {
                String errorMessage = violations.stream().map(violation -> violation.getMessage() + "; ").collect(Collectors.joining());
                return Mono.error(new ValidationException("Validation failed: " + errorMessage));
            }

            return userRepository.save(user);
        }).onErrorMap(DuplicateKeyException.class,
                error -> new UserAlreadyExistsException("Email or username already exists"));
    }

    public Mono<UserDetails> validateUserByEmailAndPassword(String email, String password) throws BadCredentialsException {
        return userRepository.findByEmail(email).switchIfEmpty(Mono.error(new BadCredentialsException("Invalid credentials"))).filter(user -> user.isEnabled() && user.isAccountNonLocked() && passwordEncoder.matches(password, user.getPassword())).switchIfEmpty(Mono.error(new BadCredentialsException("Invalid credentials")));
    }

    public Mono<User> findUserById(String userId) {
        return userRepository.findById(Long.valueOf(userId)).switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")));
    }

    public Mono<User> updateProfile(String userId, UpdateProfileRequest request) {
        return findUserById(userId).flatMap(user -> {
            Mono<Boolean> usernameTaken = request.getUsername().equals(user.getUsername()) ? Mono.just(false)
                    : userRepository.findByUsername(request.getUsername())
                    .map(other -> !((User) other).getId().equals(user.getId()))
                    .defaultIfEmpty(false);
            return usernameTaken.flatMap(taken -> {
                if (taken) {
                    return Mono.error(new UserAlreadyExistsException("Username already exists"));
                }
                user.setUsername(request.getUsername());
                user.setFirstName(blankToNull(request.getFirstName()));
                user.setLastName(blankToNull(request.getLastName()));
                user.setBirthday(request.getBirthday());
                user.setLinks(ProfileLinks.encode(request.getLinks()));
                user.setBackground(request.getBackground());
                return userRepository.save(user);
            });
        }).onErrorMap(DuplicateKeyException.class, error -> new UserAlreadyExistsException("Username already exists"));
    }

    public Mono<User> changePassword(String userId, String currentPassword, String newPassword) {
        return findUserById(userId).flatMap(user -> {
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return Mono.error(new BadCredentialsException("Current password is incorrect"));
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            return userRepository.save(user);
        });
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }


}
