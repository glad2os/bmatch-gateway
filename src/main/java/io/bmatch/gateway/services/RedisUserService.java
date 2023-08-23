package io.bmatch.gateway.services;

import io.bmatch.gateway.dto.UserProfile;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class RedisUserService {

    private final ReactiveRedisTemplate<String, UserProfile> userRedisTemplate;

    private final UserService userService;

    public RedisUserService(UserService userService, ReactiveRedisTemplate<String, UserProfile> userRedisTemplate) {
        this.userService = userService;
        this.userRedisTemplate = userRedisTemplate;
    }

    public Mono<UserProfile> findUserById(String id) {
        String key = "user_profile:" + id;

        return userRedisTemplate.opsForValue().get(key).cast(UserProfile.class).flatMap(existingUser -> userRedisTemplate.expire(key, Duration.ofHours(1)).then(Mono.just(existingUser))).switchIfEmpty(Mono.defer(() -> userService.findUserById(id).flatMap(user -> {
            UserProfile userProfile = UserProfile.ofUser(user);
            return userRedisTemplate.opsForValue().set(key, userProfile, Duration.ofHours(1)).thenReturn(userProfile);
        })));
    }

}
