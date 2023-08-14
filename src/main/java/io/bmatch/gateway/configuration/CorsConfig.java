package io.bmatch.gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${bmatch.isProduction:false}")
    private boolean isProduction;

    private final DiscoveryClient discoveryClient;

    public CorsConfig(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        List<ServiceInstance> instances = discoveryClient.getInstances("svelte-app");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.setAllowCredentials(true);
        if (isProduction) {
            corsConfiguration.addAllowedOrigin(String.format("http://%s:%s", instances.get(0).getHost(), instances.get(0).getPort()));
        } else {
            corsConfiguration.addAllowedOrigin("http://localhost:5173");
            corsConfiguration.addAllowedOrigin("http://127.0.0.1:5173");
        }
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");

        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(source);
    }

}