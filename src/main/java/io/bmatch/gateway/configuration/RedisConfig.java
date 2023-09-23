package io.bmatch.gateway.configuration;

import io.bmatch.gateway.dto.UserProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
public class RedisConfig {
    @Value("${bmatch.isProduction}")
    private boolean isProduction;


    @Value("${bmatch.redis.host}")
    private String redisHost;

    @Value("${bmatch.redis.port}")
    private int redisPort;

    private final DiscoveryClient discoveryClient;

    public RedisConfig(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Primary
    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        if (!isProduction) {
            System.out.println("Connecting to DEV Redis at: " + redisHost + ":" + redisPort);
            return new LettuceConnectionFactory(redisHost, redisPort);
        }

        List<ServiceInstance> instances = discoveryClient.getInstances("redis");
        System.out.println(instances);

        if (instances.isEmpty()) {
            throw new RuntimeException("Cannot find redis service from Consul");
        }

        String host = instances.get(0).getHost();
        int port = instances.get(0).getPort();

        System.out.println("Connecting PROD to Redis at: " + host + ":" + port);

        return new LettuceConnectionFactory(host, port);
    }


    @Bean
    public ReactiveRedisTemplate<String, UserProfile> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<UserProfile> serializer = new Jackson2JsonRedisSerializer<>(UserProfile.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, UserProfile> builder = RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, UserProfile> context = builder.value(serializer).build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

}
