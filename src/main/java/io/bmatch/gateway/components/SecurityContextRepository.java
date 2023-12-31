package io.bmatch.gateway.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import io.bmatch.gateway.configuration.GlobalExceptionHandler;
import io.bmatch.gateway.services.JwtService;
import io.bmatch.gateway.services.UserService;
import reactor.core.publisher.Mono;

import java.util.Date;

@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;


    public SecurityContextRepository(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    private static final String BEARER = "Bearer ";
    private static final String EXPIRED_TOKEN = "Expired token";
    private static final String USER_NOT_FOUND = "User not found";

    @Override
    public Mono<Void> save(ServerWebExchange swe, SecurityContext sc) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            return Mono.empty();
        }

        String authToken = authHeader.substring(BEARER.length());

        try {
            Claims claimsFromToken = jwtService.getClaimsFromToken(authToken);

            if (claimsFromToken.getExpiration().before(new Date())) {
                return Mono.error(new RuntimeException(EXPIRED_TOKEN));
            }

            String userId = claimsFromToken.getSubject();

            return userService.findUserById(userId).flatMap(user -> {
                Authentication auth = new UsernamePasswordAuthenticationToken(user.getId(), null, user.getAuthorities());
                return this.authenticationManager.authenticate(auth).map(SecurityContextImpl::new).cast(SecurityContext.class);
            }).onErrorResume(e -> Mono.error(new AuthenticationException(e.getMessage()) {
            }));

        } catch (RuntimeException e) {
            try {
                return unauthorizedResponse(exchange, e.getMessage());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private <T> Mono<T> unauthorizedResponse(ServerWebExchange swe, String msg) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ServerHttpResponse response = swe.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        GlobalExceptionHandler.ErrorResponse errorResponse = new GlobalExceptionHandler.ErrorResponse(msg);

        try {
            byte[] data = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(data);
            return response.writeWith(Mono.just(buffer)).then(Mono.empty());
        } catch (JsonProcessingException e) {
            System.out.println("Error serializing errorResponse: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

}
