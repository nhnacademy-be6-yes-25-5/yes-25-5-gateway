package com.nhnacademy.apigateway.exception;

import com.nhnacademy.apigateway.exception.payload.ErrorStatus;

public class RefreshTokenFailedException extends ApplicationException {

    private ErrorStatus errorStatus;

    public RefreshTokenFailedException(ErrorStatus errorStatus) {
        super(errorStatus);
    }

}
