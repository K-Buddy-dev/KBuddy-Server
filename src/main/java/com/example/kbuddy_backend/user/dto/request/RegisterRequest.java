package com.example.kbuddy_backend.user.dto.request;

import com.example.kbuddy_backend.user.constant.Country;
import com.example.kbuddy_backend.user.constant.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

//todo: validation
public record RegisterRequest(@NotNull @Pattern(regexp = "^[a-zA-Z0-9]{3,20}$",message = "닉네임 유효성 검사 실패") String userId, @NotNull @Email String email, @NotNull @Size(min = 8) @Pattern(regexp = "^(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$") String password, @NotNull @Size(max = 30) @Pattern(regexp = "^[a-zA-Z]+$") String firstName, @NotNull @Size(max = 30) @Pattern(regexp = "^[a-zA-Z]+$") String lastName,
							  @NotNull Country country, @NotNull Gender gender) {
	public static RegisterRequest of(String userId, String email, String password, String firstName, String lastName,
		Country country, Gender gender) {
		return new RegisterRequest(userId, email, password, firstName, lastName, country, gender);
	}
}
