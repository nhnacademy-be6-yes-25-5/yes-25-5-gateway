package com.nhnacademy.apigateway.exception;

import com.nhnacademy.apigateway.exception.payload.ErrorStatus;

public class ExpireAccessJwtException extends ApplicationException {

    public String accessToken;

    public ExpireAccessJwtException(ErrorStatus errorStatus, String token) {
        super(errorStatus);
        this.accessToken = token;
    }

}
