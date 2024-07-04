package com.nhnacademy.apigateway.application.service;

import com.nhnacademy.apigateway.presentation.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.nhnacademy.apigateway.infrastructure.adaptor.AuthAdaptor;

@Service
@RequiredArgsConstructor
public class TokenService {
    @Lazy
    private final AuthAdaptor authAdaptor;

    public ResponseEntity<AuthResponse> updateAccessToken(String expiredAccessJwt) {
        return authAdaptor.refreshAccessToken(expiredAccessJwt);
    }

}
