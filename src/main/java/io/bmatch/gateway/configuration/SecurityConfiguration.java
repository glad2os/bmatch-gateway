package io.bmatch.gateway.configuration;

import io.bmatch.gateway.components.AuthenticationManager;
import io.bmatch.gateway.components.SecurityContextRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import io.bmatch.gateway.enums.PermissionLevel;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfiguration {

    private final SecurityContextRepository securityContextRepository;
    private final AuthenticationManager authenticationManager;


    public SecurityConfiguration(SecurityContextRepository securityContextRepository, AuthenticationManager authenticationManager) {
        this.securityContextRepository = securityContextRepository;
        this.authenticationManager = authenticationManager;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((swe, e1) -> Mono.fromRunnable(() -> {
                            swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        }))
                        .accessDeniedHandler((swe, e1) -> Mono.fromRunnable(() -> {
                            swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        })))
                .securityContextRepository(securityContextRepository)
                .authenticationManager(authenticationManager)

                .authorizeExchange(ae -> ae
                        .pathMatchers("/api/admin/**").hasRole(PermissionLevel.ADMINISTRATOR.name())
                        .pathMatchers("/api/user/create").permitAll()
                        .pathMatchers("/api/user/login").permitAll()
                        .pathMatchers("/api/user/**").hasAuthority(PermissionLevel.PLAYER.getPermission())
                        .pathMatchers("/api/restaurant/**").hasAuthority(PermissionLevel.PLAYER.getPermission())
                        .pathMatchers("/actuator/health").permitAll()
                        .anyExchange().authenticated())
                .build();
    }

}
