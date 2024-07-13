package com.nhnacademy.apigateway.infrastructure.adaptor;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import com.nhnacademy.apigateway.presentation.dto.response.AuthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import com.nhnacademy.apigateway.presentation.dto.response.JwtAuthResponse;

@FeignClient(name = "authAdaptor", url = "${api.authority-server}/auth")
public interface AuthAdaptor {

    @GetMapping("/refresh")
    ResponseEntity<AuthResponse> refreshAccessToken(@RequestHeader("Refresh-Token") String refreshJwt);

    @GetMapping("/info")
    JwtAuthResponse getUserInfoByUUID(@RequestParam String uuid);

}