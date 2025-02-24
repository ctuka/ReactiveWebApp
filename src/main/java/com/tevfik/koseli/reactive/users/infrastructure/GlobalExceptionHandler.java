package com.tevfik.koseli.reactive.users.infrastructure;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@RestControllerAdvice  //@ControllerAdvice annotation mostly
// used for All Spring application if you use it with rest you must add
// @Response body annotation manually there is no need for
// @RestControllerAdvice annotation something like that
public class GlobalExceptionHandler {
    @ExceptionHandler(value = DuplicateKeyException.class)
    public Mono<ErrorResponse> handleDuplicateKeyException(DuplicateKeyException exception) {
       return Mono.just(ErrorResponse.builder(exception, HttpStatus.CONFLICT, exception.getMessage()).build());
    }

    @ExceptionHandler(value = Exception.class)
    public Mono<ErrorResponse> handleGeneralException(Exception exception) {
        return Mono.just(ErrorResponse.builder(exception, HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage()).build());
    }

    @ExceptionHandler(value = WebExchangeBindException.class)
    public Mono<ErrorResponse> handleWebExchangeBindException(WebExchangeBindException exception) {
        String errorMessage = exception.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return Mono.just(ErrorResponse.builder(exception, HttpStatus.BAD_REQUEST, errorMessage).build());
    }



}
