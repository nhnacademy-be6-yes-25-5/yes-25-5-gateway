package com.nhnacademy.apigateway.common.exception;

import com.nhnacademy.apigateway.common.exception.payload.ErrorStatus;

public class JwtException extends ApplicationException {

    public JwtException(ErrorStatus errorStatus) {
        super(errorStatus);
    }

}
