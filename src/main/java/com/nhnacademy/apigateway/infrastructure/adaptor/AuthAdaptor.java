//package com.nhnacademy.apigateway.infrastructure.adaptor;
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//@FeignClient(name = "authAdaptor", url = "${eureka.gateway}/auth")
//public interface AuthAdaptor {
//
//    @GetMapping("/test")
//    ResponseEntity<String> tokenTest();
//
//}