package com.nhnacademy.apigateway.infrastructure.adaptor;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import com.nhnacademy.apigateway.presentation.dto.response.AuthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "authAdaptor", url = "127.0.0.1:8050/auth")
public interface AuthAdaptor {

    @GetMapping("/refresh")
    ResponseEntity<AuthResponse> refreshAccessToken(@RequestHeader("Refresh-Token") String refreshJwt);

}