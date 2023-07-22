package quest.dine.gateway.services;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import quest.dine.gateway.enums.PermissionLevel;
import quest.dine.gateway.enums.Status;
import quest.dine.gateway.exceptions.UserAlreadyExistsException;
import quest.dine.gateway.model.User;
import quest.dine.gateway.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import reactor.core.publisher.Mono;

import java.util.Set;

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
        user.setRole(PermissionLevel.VIEWER);
        user.setStatus(Status.REGISTERED);

        return userRepository.findByEmail(email)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new UserAlreadyExistsException("User with email " + email + " already exists"));
                    }

                    Set<ConstraintViolation<User>> violations = validator.validate(user);
                    if (!violations.isEmpty()) {
                        StringBuilder errorMessage = new StringBuilder();
                        for (ConstraintViolation<User> violation : violations) {
                            errorMessage.append(violation.getMessage()).append("; ");
                        }
                        return Mono.error(new ValidationException("Validation failed: " + errorMessage.toString()));
                    }

                    return userRepository.save(user);
                });
    }

    public Mono<UserDetails> getUserByEmail(String email, String password) throws BadCredentialsException {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid credentials")))
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid credentials")));
    }
}
