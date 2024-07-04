package com.nhnacademy.apigateway.filter;

import com.nhnacademy.apigateway.presentation.dto.request.CreateAccessTokenRequest;
import com.nhnacademy.apigateway.exception.payload.ErrorStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import com.nhnacademy.apigateway.util.JwtUtil;
import com.nhnacademy.apigateway.application.service.TokenService;
import com.nhnacademy.apigateway.presentation.dto.response.AuthResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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

        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }

        if (path.startsWith("/orders/none")) {
            return chain.filter(exchange);
        }

        if (path.matches(".*/orders/.*/delivery.*")) {
            return chain.filter(exchange);
        }
        ServerHttpRequest request = exchange.getRequest();
        String accessJwtHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String accessJwt = accessJwtHeader.substring(7);
        String refreshJwt = request.getHeaders().getFirst("Refresh-Token");

        if (!isValidAccessJwt(accessJwtHeader, accessJwt)) {
            return chain.filter(exchange);
        }

        if (!jwtUtil.isTokenValid(accessJwt)) {
            if (jwtUtil.isTokenValid(refreshJwt)) {
                return refreshToken(accessJwtHeader)
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
     * @param accessToken 현재 액세스 토큰
     * @return 갱신된 인증 응답을 포함한 Mono
     */
    private Mono<AuthResponse> refreshToken(String accessJwtHeader) {
        return Mono.fromCallable(() -> tokenService.updateAccessToken(accessJwtHeader).getBody())
                .onErrorMap(e -> new RuntimeException("토큰 갱신 중 오류가 발생했습니다.", e));
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
        return path.equals("/auth/login") || path.equals("/auth/refresh") || path.equals("/auth/logout") || path.equals("/books/categories/root") || path.equals("/books");
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