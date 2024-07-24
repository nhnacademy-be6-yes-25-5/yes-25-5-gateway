package com.nhnacademy.apigateway.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import io.jsonwebtoken.*;
import java.util.Date;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import com.nhnacademy.apigateway.common.exception.payload.ErrorStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.apigateway.common.exception.JwtException;
import com.nhnacademy.apigateway.presentation.dto.response.JwtAuthResponse;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }


    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public JwtAuthResponse getLoginUserFromToken(String token) {
        Claims claims = parseToken(token);

        Long userId = claims.get("userId", Long.class);
        String userRole = claims.get("userRole", String.class);
        String loginStatusName = claims.get("loginStatus", String.class);

        return new JwtAuthResponse(userId, userRole, loginStatusName);
    }

    private String getSubFromExpiredToken(Claims claims) {
        return claims.getSubject();
    }

    // 추가적으로, JWT를 직접 디코딩하는 방법도 제공합니다.
    public String getSubFromTokenWithoutVerification(String token) {
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();

        String payload = new String(decoder.decode(chunks[1]));
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> claimsMap = objectMapper.readValue(payload, Map.class);
            return (String) claimsMap.get("sub");
        } catch (IOException e) {
            log.error("Failed to parse JWT payload", e);
            return null;
        }
    }

    /**
     * JWT 토큰의 만료 여부를 검사합니다.
     *
     * @param accessToken 검사할 JWT 토큰
     * @return 토큰이 유효하면 true, 만료 5초 전이거나 만료되었으면 false 반환
     */
    public boolean isTokenValid(String accessToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();

            Date expiration = claims.getExpiration();
            Instant now = Instant.now();
            Instant fiveSecondsBeforeExpiration = expiration.toInstant().minusSeconds(5);
            boolean valid = now.isBefore(fiveSecondsBeforeExpiration);
            return valid;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (SignatureException e) {
            throw new JwtException(
                    ErrorStatus.toErrorStatus("시크릿키 변경이 감지되었습니다.", 401, LocalDateTime.now())
            );
        } catch (JwtException e) {
            return false;
        }
    }
}