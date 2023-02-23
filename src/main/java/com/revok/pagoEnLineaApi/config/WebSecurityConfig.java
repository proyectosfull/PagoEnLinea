package com.revok.pagoEnLineaApi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revok.pagoEnLineaApi.config.filter.JwtAuthenticationEntryPoint;
import com.revok.pagoEnLineaApi.config.filter.JwtAuthenticationFilter;
import com.revok.pagoEnLineaApi.config.filter.JwtAuthorizationFilter;
import com.revok.pagoEnLineaApi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    // impl. required with password encoder for raw password
    // used within spring security core
    @SuppressWarnings("deprecation unused")
    public PasswordEncoder bCryptPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    // this bean replace manual injection of userService & password encoder
    @Bean
    // used within spring security core
    @SuppressWarnings("unused")
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    // used within spring security core
    @SuppressWarnings("unused")
    public SecurityFilterChain configure(HttpSecurity httpSecurity, AuthenticationManager authenticationManager,
                                         ObjectMapper objectMapper, SpringValidatorAdapter adapter, JwtUtil jwtUtil)
            throws Exception {

        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager,
                objectMapper, adapter, jwtUtil);
        JwtAuthorizationFilter jwtAuthorizationFilter = new JwtAuthorizationFilter(jwtUtil);
        jwtAuthenticationFilter.setFilterProcessesUrl("/login");


        httpSecurity.cors()// añade los permisos para cors
                .and()
                .csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.POST, "/login").permitAll()
                .requestMatchers("/public/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(jwtAuthenticationFilter)
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }
}