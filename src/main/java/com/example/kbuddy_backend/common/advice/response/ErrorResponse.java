package com.example.kbuddy_backend.common.advice.response;

import java.util.List;
import lombok.Getter;

@Getter
public class ErrorResponse {
    private final String message;
    private String code;
    private List<String> details;

    public ErrorResponse(final String message) {
        this.message = message;
    }

    public ErrorResponse(final String message, final CustomCode code) {
        this.message = String.format("Error: %s", message);
        this.code = code.getCode();
    }

    public ErrorResponse(final String message, final CustomCode code, final List<String> details) {
        this.message = String.format("Error: %s", message);
        this.code = code.getCode();
        this.details = details;
    }
}
