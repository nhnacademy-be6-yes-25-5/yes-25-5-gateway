package com.nhnacademy.apigateway.presentation.dto.response;

public record JwtAuthResponse(Long customerId,
                              String role,
                              String loginStateName) {

}
