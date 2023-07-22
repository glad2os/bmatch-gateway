package quest.dine.gateway.components;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import quest.dine.gateway.services.UserService;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationManager(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        return userService.getUserByEmail(username, password)
                .flatMap(userDetails -> {
                    if (passwordEncoder.matches(password, userDetails.getPassword())) {
                        return Mono.just((Authentication) new UsernamePasswordAuthenticationToken(username, password, userDetails.getAuthorities()));
                    } else {
                        return Mono.empty();
                    }
                })
                .switchIfEmpty(Mono.defer(() -> Mono.error(new BadCredentialsException("Invalid credentials"))));
    }

}