package io.bmatch.gateway.controller;

import io.bmatch.gateway.dto.ChangePasswordRequest;
import io.bmatch.gateway.dto.PublicProfile;
import io.bmatch.gateway.dto.UpdateProfileRequest;
import io.bmatch.gateway.dto.UserProfile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import io.bmatch.gateway.services.RedisUserService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import io.bmatch.gateway.exceptions.UserAlreadyExistsException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.bmatch.gateway.dto.ApiResponse;
import io.bmatch.gateway.dto.CreateUserRequest;
import io.bmatch.gateway.dto.LoginRequest;
import io.bmatch.gateway.model.User;
import io.bmatch.gateway.services.JwtService;
import io.bmatch.gateway.services.UserService;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@Validated
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;
    private final RedisUserService redisUserService;

    public UserController(UserService userService, JwtService jwtService, RedisUserService redisUserService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.redisUserService = redisUserService;
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<ApiResponse>> createUser(@Valid @RequestBody Mono<CreateUserRequest> createUserRequestMono) {
        return createUserRequestMono.flatMap(createUserRequest -> {
            String username = createUserRequest.getUsername();
            String password = createUserRequest.getPassword();
            String email = createUserRequest.getEmail();

            return userService.createUser(username, password, email)
                    .thenReturn(ResponseEntity.ok(new ApiResponse("User created successfully", HttpStatus.OK.value())));
        }).onErrorResume(UserAlreadyExistsException.class, error ->
                Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse(error.getMessage(), HttpStatus.CONFLICT.value()))))
          .onErrorResume(error -> error instanceof ServerWebInputException || error instanceof ValidationException,
                error -> Mono.just(ResponseEntity.badRequest()
                        .body(new ApiResponse("Invalid registration data", HttpStatus.BAD_REQUEST.value()))));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResponse>> login(@Valid @RequestBody Mono<LoginRequest> loginRequestMono) {
        return loginRequestMono.flatMap(createUserRequest -> {
            String password = createUserRequest.getPassword();
            String email = createUserRequest.getEmail();

            return userService.validateUserByEmailAndPassword(email, password).map(userDetails -> {
                String token = jwtService.generateToken(String.valueOf(((User) userDetails).getId()));
                return ResponseEntity.ok(new ApiResponse(token, HttpStatus.OK.value()));
            });
        }).onErrorResume(BadCredentialsException.class, error ->
                Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(error.getMessage(), HttpStatus.UNAUTHORIZED.value()))));
    }

    /*
    Test auth
     */

    @GetMapping("/profile")
    public Mono<ResponseEntity<UserProfile>> getProfile(Principal principal) {
        if (principal == null) {
            return Mono.just(ResponseEntity.notFound().build());
        }

        return redisUserService.findUserById(principal.getName())
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @PutMapping("/profile")
    public Mono<ResponseEntity<?>> updateProfile(Principal principal, @Valid @RequestBody Mono<UpdateProfileRequest> requestMono) {
        if (principal == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        return requestMono
                .flatMap(request -> userService.updateProfile(principal.getName(), request))
                .flatMap(user -> redisUserService.evict(user.getId()).thenReturn(user))
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(UserProfile.ofUser(user)))
                .onErrorResume(UserAlreadyExistsException.class, error ->
                        Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(new ApiResponse(error.getMessage(), HttpStatus.CONFLICT.value()))))
                .onErrorResume(error -> error instanceof ServerWebInputException || error instanceof ValidationException,
                        error -> Mono.just(ResponseEntity.badRequest()
                                .body(new ApiResponse("Invalid profile data", HttpStatus.BAD_REQUEST.value()))));
    }

    @PutMapping("/password")
    public Mono<ResponseEntity<ApiResponse>> changePassword(Principal principal, @Valid @RequestBody Mono<ChangePasswordRequest> requestMono) {
        if (principal == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        return requestMono
                .flatMap(request -> userService.changePassword(principal.getName(), request.getCurrentPassword(), request.getNewPassword()))
                .thenReturn(ResponseEntity.ok(new ApiResponse("Password changed", HttpStatus.OK.value())))
                .onErrorResume(BadCredentialsException.class, error ->
                        Mono.just(ResponseEntity.badRequest()
                                .body(new ApiResponse(error.getMessage(), HttpStatus.BAD_REQUEST.value()))))
                .onErrorResume(ServerWebInputException.class, error ->
                        Mono.just(ResponseEntity.badRequest()
                                .body(new ApiResponse("The new password must be 6-72 characters", HttpStatus.BAD_REQUEST.value()))));
    }

    // Shareable profile page: readable without signing in.
    @GetMapping("/public/{id:\\d+}")
    public Mono<ResponseEntity<PublicProfile>> getPublicProfile(@PathVariable String id) {
        return redisUserService.findUserById(id)
                .map(profile -> ResponseEntity.ok(PublicProfile.of(profile)))
                .onErrorResume(UsernameNotFoundException.class, error -> Mono.just(ResponseEntity.notFound().build()))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

}
