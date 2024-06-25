package com.nhnacademy.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class RouteLocatorConfig {

    private static final Logger logger = LoggerFactory.getLogger(RouteLocatorConfig.class);

    @Bean
    public RouteLocator myRoute(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("AUTHORITY-SERVER",
                        p->p.path("/auth/**").and()
                                .uri("lb://AUTHORITY-SERVER")
                )
                .route("COUPON-SERVER",
                        p->p.path("/coupons/**").and()
                                .uri("lb://COUPON-SERVER")
                )
                .route("BOOK-USER-SERVER",
                        p->p.path("/books").and()
                                .uri("lb://BOOK-USER-SERVER")
                )
                .route("BOOK-USER-SERVER",
                        p->p.path("/users/**", "/users").and()
                                .uri("lb://BOOK-USER-SERVER")
                )
                .route("ORDER-PAYMENT-SERVER",
                        p->p.path("/orders/**").and()
                                .uri("lb://ORDER-PAYMENT-SERVER")
                )
                .build();
    }
}