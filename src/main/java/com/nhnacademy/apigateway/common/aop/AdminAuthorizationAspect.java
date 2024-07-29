package com.nhnacademy.apigateway.common.aop;

import com.nhnacademy.apigateway.common.exception.UnauthorizedAccessException;
import com.nhnacademy.apigateway.common.exception.payload.ErrorStatus;
import com.nhnacademy.apigateway.infrastructure.adaptor.AuthAdaptor;
import com.nhnacademy.apigateway.presentation.dto.response.JwtAuthResponse;
import com.nhnacademy.apigateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
public class AdminAuthorizationAspect {

    private final JwtUtil jwtUtil;
    private final AuthAdaptor authAdaptor;

    @Around("execution(* com.nhnacademy.apigateway.filter.JwtAuthenticationGlobalFilter.filter(..))")
    public Object checkAdminAuthorization(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        ServerWebExchange exchange = (ServerWebExchange) args[0];
        String path = exchange.getRequest().getPath().value();

        if (path.contains("/admin")) {
            String accessJwtHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String accessJwt = accessJwtHeader.substring(7);
            JwtAuthResponse user = jwtUtil.getLoginUserFromToken(accessJwt);
            if (!user.role().equals("ADMIN")) {
                throw new UnauthorizedAccessException(ErrorStatus.builder()
                        .message("접근 권한이 없습니다.")
                        .status(403)
                        .timestamp(LocalDateTime.now()).build());
            }
        }

        // 요청 처리를 계속 진행합니다.
        return joinPoint.proceed();
    }
}