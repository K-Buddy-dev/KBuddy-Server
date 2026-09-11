package com.example.kbuddy_backend.common.advice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import com.example.kbuddy_backend.common.advice.response.CustomCode;
import com.example.kbuddy_backend.common.advice.response.ErrorResponse;
import com.example.kbuddy_backend.common.exception.BadRequestException;
import com.example.kbuddy_backend.common.exception.DuplicateException;
import com.example.kbuddy_backend.common.exception.NotFoundException;
import com.example.kbuddy_backend.common.exception.UnauthorizedException;

import java.util.List;

import com.example.kbuddy_backend.user.exception.AccountDeactivatedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class ControllerAdviceException {

    //@Valid 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> processValidationError(MethodArgumentNotValidException ex) {
        // 모든 필드 오류 메시지를 리스트로 수집
        List<String> errorMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage()) // 필드명 + 오류 메시지
                .toList();
        return ResponseEntity.status(BAD_REQUEST)
                .body(new ErrorResponse("Validation failed for one or more fields.",
                        CustomCode.HTTP_400, errorMessages));
    }

    //409에러 처리
    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(final Exception e) {
        log.error(e.getMessage());
        return ResponseEntity.status(CONFLICT).body(new ErrorResponse(e.getMessage(), CustomCode.HTTP_409));
    }

    //탈퇴한 계정처리
    @ExceptionHandler(AccountDeactivatedException.class)
    public ResponseEntity<ErrorResponse> handleAccountDeactivated(final Exception e) {
        return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(new ErrorResponse(e.getMessage(), CustomCode.HTTP_415));
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

    /**
     * 서비스 계층에서 던진 접근 거부 예외.
     *
     * 아래 catch-all 핸들러가 먼저 삼켜 500이 되면 Spring Security의 예외 변환이 동작하지 않으므로
     * 여기서 명시적으로 처리한다. 비로그인 요청은 401(로그인하면 접근 가능할 수 있음),
     * 로그인 상태에서의 거부는 403으로 구분한다.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(final AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());

        if (isAnonymous()) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ErrorResponse(e.getMessage(), CustomCode.HTTP_401));
        }
        return ResponseEntity.status(FORBIDDEN).body(new ErrorResponse(e.getMessage(), CustomCode.HTTP_403));
    }

    private boolean isAnonymous() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken;
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

    /**
     * 경로 변수/파라미터의 타입이 맞지 않는 요청.
     *
     * 예: GET /blog/category 처럼 숫자 자리에 문자열이 오는 경우. catch-all 로 떨어지면
     * 잘못된 요청이 500으로 보고되므로 여기서 400으로 처리한다.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(final MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch for parameter '{}'", e.getName());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("요청 값의 형식이 올바르지 않습니다.", CustomCode.HTTP_400));
    }

    /**
     * 필수 요청 파라미터 누락. 예: GET /counselor/{id}/availability 에 year 가 없는 경우.
     * catch-all 로 떨어지면 잘못된 요청이 500으로 보고된다.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(final MissingServletRequestParameterException e) {
        log.warn("Missing request parameter '{}'", e.getParameterName());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("필수 요청 값이 빠졌습니다: " + e.getParameterName(), CustomCode.HTTP_400));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(final Exception e) {
        log.error(e.getMessage());
        return ResponseEntity.status(METHOD_NOT_ALLOWED).body(new ErrorResponse(e.getMessage(), CustomCode.HTTP_405));
    }

    /**
     * 500 처리.
     *
     * 예외 메시지와 스택 트레이스는 로그에만 남긴다. 응답에 넣으면 프레임워크 구성과 내부 클래스
     * 경로가 그대로 드러나는데, 게스트 조회 API가 열리면서 비로그인 사용자도 이 응답에 닿는다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInternalServerError(final Exception e) {
        log.error("Internal Server Error: ", e);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("서버에서 예기치 못한 오류가 발생했습니다.", CustomCode.HTTP_500));
    }

}
