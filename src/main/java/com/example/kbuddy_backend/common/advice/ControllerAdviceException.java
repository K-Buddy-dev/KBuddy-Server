package com.example.kbuddy_backend.common.advice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import com.example.kbuddy_backend.common.advice.response.CustomCode;
import com.example.kbuddy_backend.common.advice.response.ErrorResponse;
import com.example.kbuddy_backend.common.exception.BadRequestException;
import com.example.kbuddy_backend.common.exception.NotFoundException;
import com.example.kbuddy_backend.common.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class ControllerAdviceException {

    //@Valid 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> processValidationError(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(BAD_REQUEST)
                .body(new ErrorResponse(ex.getBindingResult().getAllErrors().get(0).getDefaultMessage(),
                        CustomCode.HTTP_400));
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(final Exception e) {
        log.error(e.getMessage());
        return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(new ErrorResponse(e.getMessage(), CustomCode.HTTP_422));
    }

    @ExceptionHandler({UnauthorizedException.class, AuthenticationException.class})
    public ResponseEntity<ErrorResponse> unAuthorized(final Exception e) {
        log.error(e.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(new ErrorResponse(e.getMessage(), CustomCode.HTTP_401));
    }

    //404에러 처리
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(final Exception e) {
        log.error(e.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(new ErrorResponse(e.getMessage(), CustomCode.HTTP_404));
    }

    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(final Exception e) {
        log.error(e.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), CustomCode.HTTP_400));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(final Exception e) {
        log.error(e.getMessage());
        return ResponseEntity.status(METHOD_NOT_ALLOWED).body(new ErrorResponse(e.getMessage(), CustomCode.HTTP_405));
    }

    //500에러 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInternalServerError(final Exception e) {
        log.error("Internal Server Error: ", e);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("서버에서 예기치 못한 오류가 발생했습니다.", CustomCode.HTTP_500));
    }

}
