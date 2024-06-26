package com.nhnacademy.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT 토큰의 만료 여부를 검사합니다.
     *
     * @param token 검사할 JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expirationDate = claims.getExpiration();
            return expirationDate.after(new Date());
        } catch (Exception e) {
            // 토큰 파싱 중 예외 발생 시 (만료, 서명 불일치 등)
            return false;
        }
    }

    /**
     * JWT 토큰에서 특정 클레임 값을 추출합니다.
     *
     * @param token JWT 토큰
     * @param claimName 추출할 클레임의 이름
     * @return 클레임 값, 클레임이 없거나 토큰이 유효하지 않으면 null
     */
    public Object getClaimFromToken(String token, String claimName) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get(claimName);
        } catch (Exception e) {
            return null;
        }
    }
}