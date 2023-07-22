package quest.dine.gateway.components;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import quest.dine.gateway.services.JwtService;
import quest.dine.gateway.services.UserService;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationManager(UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String email = authentication.getName();

        return userService.findUserByEmail(email)
                .flatMap(userDetails -> Mono.just((Authentication) new UsernamePasswordAuthenticationToken(email, null, userDetails.getAuthorities())))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new BadCredentialsException("Invalid credentials"))));
    }

}