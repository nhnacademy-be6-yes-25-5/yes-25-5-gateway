package com.nhnacademy.apigateway.exception;

import com.nhnacademy.apigateway.exception.payload.ErrorStatus;

public class ExpireRefreshJwtException extends ApplicationException {

    public String accessToken;

    public ExpireRefreshJwtException(ErrorStatus errorStatus) {
        super(errorStatus);
    }

}
