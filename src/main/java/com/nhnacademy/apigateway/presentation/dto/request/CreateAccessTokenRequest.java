package com.nhnacademy.apigateway.presentation.dto.request;

import lombok.Builder;

@Builder
public record CreateAccessTokenRequest(String expiredAccessJwt) { }
