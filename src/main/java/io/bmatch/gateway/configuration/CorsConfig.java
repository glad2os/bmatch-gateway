package io.bmatch.gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
public class CorsConfig {
    @Value("${bmatch.isProduction}")
    private boolean isProduction;
    @Value("${bmatch.cors.origins:}")
    private String origins;

    @Bean
    public CorsWebFilter corsWebFilter() {
        String defaults = isProduction ? "http://bubblematch.io,https://bubblematch.io" :
                "http://localhost:5173,http://127.0.0.1:5173,http://192.168.2.73:5173,http://host.docker.internal:5173";
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.stream((origins.isBlank() ? defaults : origins).split(","))
                .map(String::trim).filter(origin -> !origin.isEmpty()).toList());
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
