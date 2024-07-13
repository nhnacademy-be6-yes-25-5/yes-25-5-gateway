package com.nhnacademy.apigateway.common.exception;

import com.nhnacademy.apigateway.common.exception.payload.ErrorStatus;

public class UnauthorizedAccessException extends ApplicationException {

    public UnauthorizedAccessException(ErrorStatus errorStatus) {
        super(errorStatus);
    }

}
