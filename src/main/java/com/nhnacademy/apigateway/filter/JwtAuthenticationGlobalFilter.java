package com.nhnacademy.apigateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.apigateway.application.service.TokenService;
import com.nhnacademy.apigateway.presentation.dto.response.AuthResponse;
import com.nhnacademy.apigateway.util.JwtUtil;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * JWT 인증을 처리하는 글로벌 필터 클래스입니다.
 * 이 필터는 모든 요청에 대해 JWT 토큰을 검증하고, 필요한 경우 토큰을 갱신합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationGlobalFilter implements WebFilter {

    private final ObjectMapper objectMapper;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;


    /**
     * 요청을 필터링하고 JWT 인증을 처리합니다.
     *
     * @param exchange 현재 서버 웹 교환
     * @param chain 필터 체인
     * @return 처리된 요청에 대한 Mono
     */

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        ServerHttpRequest request = exchange.getRequest();

        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }

        if (path.startsWith("/orders/none")) {
            return chain.filter(exchange);
        }

//        if (path.matches("/coupons")) {
//            return chain.filter(exchange);
//        }

        if (path.matches(".*/orders/.*/delivery.*")) {
            return chain.filter(exchange);
        }

        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (Objects.isNull(authorizationHeader) && (path.startsWith("/reviews/books") || path.startsWith("/payments"))) {
            return chain.filter(exchange);
        }

        if (path.equals("/orders") && exchange.getRequest().getMethod().equals(HttpMethod.POST) && Objects.isNull(authorizationHeader)) {
            return chain.filter(exchange);
        }

        if (path.startsWith("/users/cart-books")
                && exchange.getRequest().getMethod().name().equals("POST")
                && request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION) == null) {
            return chain.filter(exchange);
        }

        if (path.matches("/books/likes/\\d+/exist")) {
            try {

                String accessJwtHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                String accessJwt = accessJwtHeader.substring(7);
                String refreshJwt = request.getHeaders().getFirst("Refresh-Token");

                return chain.filter(exchange);

            } catch(NullPointerException e) {
                return chain.filter(exchange);
            }
        }


        if (path.matches("/coupons") && request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION).isEmpty()) {
            return chain.filter(exchange);
        }


        String accessJwtHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String accessJwt = accessJwtHeader.substring(7);
        String refreshJwt = request.getHeaders().getFirst("Refresh-Token");

        if (!isValidAccessJwt(accessJwtHeader, accessJwt)) {
            return chain.filter(exchange);
        }

        if (!jwtUtil.isTokenValid(accessJwt)) {
            if (jwtUtil.isTokenValid(refreshJwt)) {
                return refreshToken(refreshJwt)
                        .flatMap(newTokens -> {
                            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + newTokens.accessToken())
                                    .header("Refresh-Token", newTokens.refreshToken())
                                    .build();
                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        })
                        .onErrorResume(e -> Mono.error(e));  // 예외를 전역 핸들러로 전파
            } else {
                return createAuthenticationErrorResponse(exchange, "refresh 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED.value());
            }
        }

        return chain.filter(exchange);
    }

    /**
     * 액세스 토큰을 갱신합니다.
     *
     //     * @param accessToken 현재 액세스 토큰
     * @return 갱신된 인증 응답을 포함한 Mono
     */
    private Mono<AuthResponse> refreshToken(String refreshJwt) {
        return Mono.fromCallable(() -> {
                    log.info("Refreshing access token...");
                    AuthResponse newTokens = tokenService.updateAccessToken(refreshJwt).getBody();
                    log.info("Access token refreshed: {}", newTokens.accessToken());
                    return newTokens;
                })
                .onErrorMap(e -> {
                    log.error("Error refreshing token: {}", e.getMessage());
                    return new RuntimeException("토큰 갱신 중 오류가 발생했습니다.", e);
                });
    }

    /**
     * 인증 오류 응답을 생성합니다.
     *
     * @param exchange 현재 서버 웹 교환
     * @param message 오류 메시지
     * @param status HTTP 상태 코드
     * @return 오류 응답을 포함한 Mono
     */
    private Mono<Void> createAuthenticationErrorResponse(ServerWebExchange exchange, String message, int status) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", status);
        responseBody.put("message", message);
        responseBody.put("timestamp", LocalDateTime.now().toString());

        exchange.getResponse().setStatusCode(HttpStatus.valueOf(status));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] responseBytes = objectMapper.writeValueAsBytes(responseBody);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(responseBytes)));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    /**
     * 주어진 경로가 인증에서 제외되는지 확인합니다.
     *
     * @param path 요청 경로
     * @return 경로가 제외되면 true, 그렇지 않으면 false
     */
    private boolean isExcludedPath(String path) {
        return path.equals("/auth/login") || path.equals("/auth/refresh") || path.equals("/auth/logout")
            || path.equals("/books/categories/root") || (path.startsWith("/books") && !path.startsWith("/books/likes"))
                || path.matches("/books/likes/books/\\d+") || path.equals("/users/check-email") || path.equals("/users/sign-up")
                || path.equals("/users/find/password") || path.equals("/users/find/email")
            || path.startsWith("/users/cart-books") || path.startsWith("/policies") || path.startsWith("/auth/dormant");
    }

    /**
     * JWT 헤더의 형식을 검사합니다.
     *
     * @param accessJwtHeader Authorization 헤더 값
     * @return 헤더가 유효한 경우 true, 그렇지 않으면 false
     */
    public boolean isJwtHeaderForm(String accessJwtHeader) {
        return accessJwtHeader != null && accessJwtHeader.startsWith("Bearer ");
    }

    /**
     * AccessJWT 헤더의 유효성을 검사합니다.
     *
     * @param accessJwtHeader Authorization 헤더 값
     * @param accessJwt       Access JWT 문자열
     * @return 헤더와 토큰이 모두 유효한 경우 true, 그렇지 않으면 false
     */
    public boolean isValidAccessJwt(String accessJwtHeader, String accessJwt) {
        return (isJwtHeaderForm(accessJwtHeader) || jwtUtil.isTokenValid(accessJwt));
    }
}