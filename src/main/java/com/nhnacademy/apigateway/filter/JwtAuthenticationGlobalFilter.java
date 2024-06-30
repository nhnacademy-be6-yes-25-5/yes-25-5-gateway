package com.nhnacademy.apigateway.filter;

import com.nhnacademy.apigateway.exception.payload.ErrorStatus;
import com.nhnacademy.apigateway.util.JwtUtil;
import com.nhnacademy.apigateway.exception.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationGlobalFilter implements GlobalFilter {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getPath().value();

        // 로그인 요청 경로는 제외
        if (path.startsWith("/auth/login")) {
            return chain.filter(exchange);
        }

        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

            String token = authorizationHeader.substring(7);

            if (jwtUtil.isTokenValid(token)) {
                return chain.filter(exchange);
            } else {
                throw new JwtException(ErrorStatus.toErrorStatus("토큰이 만료되었습니다.", 401, LocalDateTime.now()));
            }
        }

        // TODO Authorization 헤더가 없거나 Bearer 토큰이 아닌 경우 -> 로그인페이지
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

        return exchange.getResponse().setComplete();
    }

}