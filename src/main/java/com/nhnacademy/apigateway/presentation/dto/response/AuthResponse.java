package com.nhnacademy.apigateway.presentation.dto.response;

import lombok.Builder;

@Builder
public record AuthResponse(String accessToken, String refreshToken){ }
