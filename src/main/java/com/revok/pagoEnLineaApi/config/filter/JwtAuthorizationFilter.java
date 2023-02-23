package com.revok.pagoEnLineaApi.config.filter;

import com.revok.pagoEnLineaApi.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Log4j2
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final String BEARER = "Bearer ";
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request.getServletPath().equals("/login") || request.getServletPath().equals("/public/refreshToken")) {
            chain.doFilter(request, response);
            return;
        }
        final String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER)) {
            log.debug("JWT Token does not begin with Bearer String");
            chain.doFilter(request, response);
            return;
        }
        UsernamePasswordAuthenticationToken authenticationToken = null;
        JwtUtil.VerifyTokenResult verifyResult = jwtUtil.verifyToken(authorizationHeader.substring(BEARER.length()));
        if (verifyResult.isErrorPresent()) {
            response.addHeader("error", verifyResult.getError());
        } else {
            authenticationToken = verifyResult.getToken();
        }
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        chain.doFilter(request, response);
    }
}