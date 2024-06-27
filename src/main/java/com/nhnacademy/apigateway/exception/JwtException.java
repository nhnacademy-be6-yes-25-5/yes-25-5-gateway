package com.nhnacademy.apigateway.exception;

import com.nhnacademy.apigateway.exception.payload.ErrorStatus;

public class JwtException extends ApplicationException {

    public JwtException(ErrorStatus errorStatus) {
        super(errorStatus);
    }

}
