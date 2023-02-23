package com.revok.pagoEnLineaApi.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.revok.pagoEnLineaApi.model.Propietario;
import com.revok.pagoEnLineaApi.service.PropietarioService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private final Algorithm signToken;
    private final Algorithm signRefreshToken;
    private final PropietarioService propietarioService;
    private final int tokenExpirationTime = 1000 * 60 * 60 * 8; // 8 hours
    private final int refreshTokenExpirationTime = 1000 * 60 * 60 * 24 * 2; // 2 weeks

    public JwtUtil(@Value("${key.secret}") String keySecret, @Value("${key.secretRefresh}") String keySecretRefresh, PropietarioService propietarioService) {
        this.signToken = Algorithm.HMAC512(keySecret);
        this.signRefreshToken = Algorithm.HMAC512(keySecretRefresh);
        this.propietarioService = propietarioService;
    }

    public String getRefreshedToken(String refreshToken) {
        JWTVerifier verifier = JWT.require(signRefreshToken).build();
        DecodedJWT decodedJWT = verifier.verify(refreshToken);
        String username = decodedJWT.getSubject();
        Propietario user = propietarioService.loadUserByUsername(username);
        long now = System.currentTimeMillis();
        return JWT.create()
                .withSubject(user.getUsername())
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now + tokenExpirationTime))
                .sign(signToken);
    }

    public String getToken(Propietario user) {
        long now = System.currentTimeMillis();
        return JWT.create()
                .withSubject(user.getNombreCompleto())
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now + tokenExpirationTime))
                .sign(signToken);
    }

    public String getRefreshToken(Propietario user) {
        long now = System.currentTimeMillis();
        return JWT.create()
                .withSubject(user.getUsername())
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now + refreshTokenExpirationTime))
                .sign(signRefreshToken);
    }

    public VerifyTokenResult verifyToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(signToken).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            String username = decodedJWT.getSubject();
            return new VerifyTokenResult(new UsernamePasswordAuthenticationToken(username, null, null));
        } catch (Exception e) {
            return new VerifyTokenResult(e.getMessage());
        }
    }

    @Getter
    @Setter
    public static class VerifyTokenResult {
        private final UsernamePasswordAuthenticationToken token;
        private final String error;
        private final boolean errorPresent;

        public VerifyTokenResult(UsernamePasswordAuthenticationToken token) {
            this.token = token;
            this.errorPresent = false;
            this.error = null;
        }

        public VerifyTokenResult(String error) {
            this.token = null;
            this.errorPresent = true;
            this.error = error;
        }
    }
}
