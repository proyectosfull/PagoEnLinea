package com.revok.pagoEnLineaApi.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
public class BeanInitializerConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addExposedHeader("authorization");
        config.addExposedHeader("error");
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        final int HTTP_CONNECT_TIMEOUT = 45000;
        final int HTTP_READ_TIMEOUT = 45000;
        return builder
                .setConnectTimeout(Duration.of(HTTP_CONNECT_TIMEOUT, ChronoUnit.MILLIS))
                .setReadTimeout(Duration.of(HTTP_READ_TIMEOUT, ChronoUnit.MILLIS))
                .build();
    }
}
