package com.nhnacademy.apigateway.util;

import com.nhnacademy.apigateway.exception.JwtException;
import com.nhnacademy.apigateway.exception.payload.ErrorStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT 토큰의 만료 여부를 검사합니다.
     *
     * @param token 검사할 JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean isTokenValid(String token) {
        try {
            Jws<Claims> claimJets = Jwts.parser().setSigningKey(secretKey)
                    .parseClaimsJws(token);

            Claims claims = claimJets.getBody();

            if (claims.getExpiration().before(new Date())) {
                throw new JwtException(
                        ErrorStatus.toErrorStatus("토큰의 유효시간이 지났습니다.", 401, LocalDateTime.now())
                );
            }

            return true;
        } catch (SignatureException e) {
            throw new JwtException(
                    ErrorStatus.toErrorStatus("시크릿키 변경이 감지되었습니다.", 401, LocalDateTime.now())
            );
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