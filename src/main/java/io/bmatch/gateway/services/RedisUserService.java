package io.bmatch.gateway.services;

import io.bmatch.gateway.dto.UserProfile;
import io.bmatch.gateway.model.User;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class RedisUserService {

    private final ReactiveRedisOperations<String, UserProfile> userOps;

    private final UserService userService;

    public RedisUserService(ReactiveRedisOperations<String, UserProfile> userOps, UserService userService) {
        this.userOps = userOps;
        this.userService = userService;
    }

    public Mono<UserProfile> findUserById(String id) {
        return userOps.opsForValue().get(id)
                .flatMap(existingUser -> userOps.expire(id, Duration.ofHours(1)).then(Mono.just(existingUser)))
                .switchIfEmpty(Mono.defer(() -> {
                    // Обращаемся к источнику данных, если значения нет в Redis
                    return userService.findUserById(id)
                            .map(UserProfile::ofUser)
                            .flatMap(userProfile -> {
                                // Сохраняем результат в Redis с TTL 1 час
                                return userOps.opsForValue().set(id, userProfile, Duration.ofHours(1))
                                        .thenReturn(userProfile);
                            });
                }));
    }

}
