package com.nhnacademy.apigateway.common.exception;

import com.nhnacademy.apigateway.common.exception.payload.ErrorStatus;
import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {

    private final ErrorStatus errorStatus;

    public ApplicationException(ErrorStatus errorStatus) {
        this.errorStatus = errorStatus;
    }

}
