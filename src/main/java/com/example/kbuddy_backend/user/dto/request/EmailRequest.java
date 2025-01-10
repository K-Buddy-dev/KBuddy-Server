package com.example.kbuddy_backend.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record EmailRequest(@NotNull @Email String email) {
}
