package com.nhnacademy.apigateway.common.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.apigateway.common.exception.ApplicationException;
import com.nhnacademy.apigateway.common.exception.payload.ErrorStatus;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalErrorWebExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        return handleApplicationException(exchange, (ApplicationException) ex);
    }

    private Mono<Void> handleApplicationException(ServerWebExchange exchange, ApplicationException ex) {
        ErrorStatus errorStatus = ex.getErrorStatus();
        exchange.getResponse().setStatusCode(errorStatus.toHttpStatus());
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return exchange.getResponse().writeWith(Mono.fromCallable(() -> {
            byte[] bytes = objectMapper.writeValueAsBytes(errorStatus);
            return exchange.getResponse().bufferFactory().wrap(bytes);
        }));
    }
}