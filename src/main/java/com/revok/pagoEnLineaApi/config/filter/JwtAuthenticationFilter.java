package com.revok.pagoEnLineaApi.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revok.pagoEnLineaApi.model.Propietario;
import com.revok.pagoEnLineaApi.model.dto.in.LoginDTO;
import com.revok.pagoEnLineaApi.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import java.io.IOException;

@RequiredArgsConstructor
@Log4j2
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final SpringValidatorAdapter adapter;
    private final JwtUtil jwtUtil;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (!request.getMethod().equals(HttpMethod.POST.name())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        try {
            LoginDTO credentials = objectMapper.readValue(request.getInputStream(), LoginDTO.class);
            BeanPropertyBindingResult result = new BeanPropertyBindingResult(credentials, "credentials");
            adapter.validate(credentials, result);
            if (result.hasErrors())
                throw new AuthenticationServiceException("Bad credentials format");
            log.debug(credentials.getFullname());
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                    credentials.getFullname(), "");
            logger.debug("Auth in process");
            return authenticationManager.authenticate(authRequest);
        } catch (IOException e) {
            logger.debug("Auth error in " + e.getMessage());
            throw new AuthenticationServiceException("Error parsing credentials format");
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) {
        logger.debug("Auth success");
        Propietario user = (Propietario) authResult.getPrincipal();
        String token = jwtUtil.getToken(user);
        String refreshToken = jwtUtil.getRefreshToken(user);
        final String bearer = "Bearer ";
        response.addHeader("Authorization", bearer + token);
        response.addHeader("refresh_token", bearer + refreshToken);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) {
        logger.debug("Auth failed");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.addHeader("error", failed.getMessage());
    }
}