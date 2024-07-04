package com.nhnacademy.apigateway.exception;

import com.nhnacademy.apigateway.exception.payload.ErrorStatus;

public class UuidMisMatchException extends ApplicationException {

    private ErrorStatus errorStatus;

    public UuidMisMatchException(ErrorStatus errorStatus) {
        super(errorStatus);
    }

}
