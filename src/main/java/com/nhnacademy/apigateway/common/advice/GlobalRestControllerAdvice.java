package com.nhnacademy.apigateway.common.advice;

import com.nhnacademy.apigateway.common.exception.payload.ErrorStatus;
import com.nhnacademy.apigateway.common.exception.ApplicationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalRestControllerAdvice {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorStatus> handleJwtException(ApplicationException e) {
        ErrorStatus errorStatus = e.getErrorStatus();

        return new ResponseEntity<>(errorStatus, errorStatus.toHttpStatus());
    }
}
