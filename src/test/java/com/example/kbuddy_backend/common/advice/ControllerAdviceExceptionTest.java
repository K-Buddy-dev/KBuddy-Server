package com.example.kbuddy_backend.common.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.kbuddy_backend.common.advice.response.CustomCode;
import com.example.kbuddy_backend.common.advice.response.ErrorResponse;
import com.example.kbuddy_backend.common.exception.IdempotencyConflictException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

class ControllerAdviceExceptionTest {

    private final ControllerAdviceException controllerAdvice = new ControllerAdviceException();

    @Test
    void optimisticLockConflictReturns409() {
        ObjectOptimisticLockingFailureException exception =
                new ObjectOptimisticLockingFailureException("Payment", 1L);

        ResponseEntity<ErrorResponse> response = controllerAdvice.handleOptimisticLock(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(CustomCode.HTTP_409.getCode(), response.getBody().getCode());
        assertEquals(
                "Error: 다른 요청이 먼저 처리되었습니다. 최신 상태를 확인해주세요.",
                response.getBody().getMessage());
    }

    @Test
    void idempotencyConflictReturns409() {
        IdempotencyConflictException exception =
                new IdempotencyConflictException("동일한 멱등키가 다른 요청에 사용되었습니다.");

        ResponseEntity<ErrorResponse> response =
                controllerAdvice.handleIdempotencyConflict(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(CustomCode.HTTP_409.getCode(), response.getBody().getCode());
    }
}
