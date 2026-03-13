package org.example.exception;

import org.example.vo.base.ApiResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ApiResult<String> handleException(Exception e) {
        e.printStackTrace();
        return ApiResult.fail(e.getMessage());
    }
}
