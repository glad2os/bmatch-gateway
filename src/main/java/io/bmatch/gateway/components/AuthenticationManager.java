package io.bmatch.gateway.components;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import io.bmatch.gateway.services.UserService;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {
    private final UserService userService;

    public AuthenticationManager(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String userId = authentication.getName();

        return userService.findUserById(userId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new BadCredentialsException("Invalid credentials"))))
                .flatMap(userDetails -> {
                    if (!userDetails.isAccountNonLocked()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User account is locked"));
                    }
                    if (!userDetails.isAccountNonExpired()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User account has expired"));
                    }
                    if (!userDetails.isCredentialsNonExpired()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User credentials have expired"));
                    }
                    if (!userDetails.isEnabled()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User account disabled"));
                    }
                    return Mono.just((Authentication) new UsernamePasswordAuthenticationToken(userId, null, userDetails.getAuthorities()));
                })
                .onErrorResume(BadCredentialsException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad Credentials")));
    }


}