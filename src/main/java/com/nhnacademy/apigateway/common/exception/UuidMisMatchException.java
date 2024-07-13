package com.nhnacademy.apigateway.common.exception;

import com.nhnacademy.apigateway.common.exception.payload.ErrorStatus;

public class UuidMisMatchException extends ApplicationException {

    private ErrorStatus errorStatus;

    public UuidMisMatchException(ErrorStatus errorStatus) {
        super(errorStatus);
    }

}
