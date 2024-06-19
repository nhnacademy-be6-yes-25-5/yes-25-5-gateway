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
                .route("AUTH-SERVER",
                        p->p.path("/auth/**").and()
                                .uri("lb://AUTH-SERVER")
                )

            .route("ORDER-PAYMENT-SERVER",
                p -> p.path("/orders/**", "/payments/**")
                    .or()
                    .path("/policies/shipping/**", "/policies/takeout" , "/policies/returns/**")
                    .uri("lb://ORDER-PAYMENT-SERVER"))
                .build();
    }
}