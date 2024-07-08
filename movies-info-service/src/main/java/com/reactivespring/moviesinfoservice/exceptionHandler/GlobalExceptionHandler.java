package com.reactivespring.moviesinfoservice.exceptionHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<String> handleRequestBodyError(WebExchangeBindException ex){
        log.error("\nError caught in exceptionHandler : {} \n{}",ex.getMessage(), ex);
        var defaultErrorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).sorted()
                .collect(Collectors.joining(", "));
        log.error("\nActual Error is {}",defaultErrorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(defaultErrorMessage);
    }
}
