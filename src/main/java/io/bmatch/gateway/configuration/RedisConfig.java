package io.bmatch.gateway.configuration;

import io.bmatch.gateway.dto.UserProfile;
import io.bmatch.gateway.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Primary
    @Bean
    ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

    @Bean
    ReactiveRedisTemplate<String, UserProfile> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<UserProfile> serializer = new Jackson2JsonRedisSerializer<>(UserProfile.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, UserProfile> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, UserProfile> context = builder.value(serializer).build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
