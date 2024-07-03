package com.nhnacademy.apigateway.filter;

import com.nhnacademy.apigateway.presentation.dto.request.CreateAccessTokenRequest;
import com.nhnacademy.apigateway.exception.payload.ErrorStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.apigateway.exception.RefreshTokenFailedException;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationGlobalFilter implements WebFilter {

    private final ObjectMapper objectMapper;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 필터 적용이 필요없이 라우팅이 필요한 URL은 isExcludedPath() 하위에 적어주세요.
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

        //access 없거나 유효하지 않음
        if (!isValidAccessJwt(accessJwtHeader, accessJwt)) {
            //refresh가 있고 유효함 -> access Jwt 재발급 요청
            if (isValidRefreshJwt(refreshJwt)) {
                return refreshToken(accessJwt)
                        .flatMap(newTokens -> {
                            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + newTokens.accessJwt())
                                    .header("Refresh-Token", newTokens.refreshJwt())
                                    .build();
                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        })
                        .onErrorResume(e -> {
                            log.error("Token refresh error", e);
                            return createAuthenticationErrorResponse(exchange, "토큰 갱신 중 오류가 발생했습니다.", HttpStatus.NOT_FOUND.value());
                        });
            }
            //refresh가 없거나 유효하지 않음 -> 로그인 요청
            else {
                return createAuthenticationErrorResponse(exchange, "refresh 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED.value());
            }
        }

        return chain.filter(exchange);
    }

    /**
     * 주어진 경로가 JWT 인증에서 제외되는 경로인지 확인합니다.
     *
     * @param path 확인할 요청 경로
     * @return 제외 경로인 경우 true, 그렇지 않으면 false
     */
    private boolean isExcludedPath(String path) {
        return path.equals("/auth/login");
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
        return isJwtHeaderForm(accessJwtHeader) && jwtUtil.isTokenValid(accessJwt);
    }

    /**
     * Refresh JWT 헤더의 유효성을 검사합니다.
     *
     * @param refreshJwt Refresh JWT 문자열
     * @return 헤더가 유효한 경우 true, 그렇지 않으면 false
     */
    public boolean isValidRefreshJwt(String refreshJwt) {
        return refreshJwt != null && jwtUtil.isTokenValid(refreshJwt);
    }

    /**
     * 토큰을 재발급합니다.
     *
     * @param accessToken 기존 Access Token
     * @return 새로운 토큰 정보
     */
    private Mono<AuthResponse> refreshToken(String accessToken) {
        return Mono.fromCallable(() -> tokenService.updateAccessToken(CreateAccessTokenRequest.builder().expiredAccessJwt(accessToken).build()).getBody())
                .onErrorMap(e -> {
                    log.error("Error during token refresh", e);
                    return new RefreshTokenFailedException(ErrorStatus.toErrorStatus(("토큰 갱신 중 오류가 발생했습니다: " + e.getMessage()), 404, LocalDateTime.now()));
                });
    }

    /**
     * 인증 오류 응답을 생성합니다.
     *
     * @param exchange 현재 요청 정보
     * @param message  오류 메시지
     * @param status   HTTP 상태 코드
     * @return 오류 응답
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
}