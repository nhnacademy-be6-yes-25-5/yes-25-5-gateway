package com.nhnacademy.apigateway.common.exception;

import com.nhnacademy.apigateway.common.exception.payload.ErrorStatus;

public class ExpireRefreshJwtException extends ApplicationException {

    public String accessToken;

    public ExpireRefreshJwtException(ErrorStatus errorStatus) {
        super(errorStatus);
    }

}
