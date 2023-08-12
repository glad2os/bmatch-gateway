package io.bmatch.gateway.controller;

import io.bmatch.gateway.dto.UserProfile;
import jakarta.validation.Valid;
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

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<ApiResponse>> createUser(@Valid @RequestBody Mono<CreateUserRequest> createUserRequestMono) {
        return createUserRequestMono.flatMap(createUserRequest -> {
            String username = createUserRequest.getUsername();
            String password = createUserRequest.getPassword();
            String email = createUserRequest.getEmail();

            return userService.createUser(username, password, email)
                    .thenReturn(new ResponseEntity<>(new ApiResponse("User created successfully", HttpStatus.OK.value()), HttpStatus.OK))
                    .doOnError(throwable -> new ResponseEntity<>(new ApiResponse(throwable.getMessage(), HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST));
        });
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
        });
    }

    /*
    Test auth
     */

    @GetMapping("/profile")
    public Mono<ResponseEntity<UserProfile>> getProfile(Principal principal) {
        return userService.findUserById(principal.getName()).map(UserProfile::ofUser)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
