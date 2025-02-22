package com.example.kbuddy_backend.user.dto.request;

import com.example.kbuddy_backend.user.constant.Country;
import com.example.kbuddy_backend.user.constant.Gender;
import com.example.kbuddy_backend.user.constant.OAuthCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

//todo: validation

public record OAuthRegisterRequest(@NotNull @Pattern(regexp = "^[a-zA-Z0-9]{3,20}$",message = "닉네임 유효성 검사 실패") String userId, @NotNull @Email(message = "이메일 형식에 맞지 않습니다.") String email, @NotNull String firstName, @NotNull String lastName,
								   @NotNull Country country, @NotNull Gender gender, @NotNull String oAuthUid, @NotNull OAuthCategory oAuthCategory) {
	public static OAuthRegisterRequest of(String userId, String email, String firstName, String lastName,
                                          Country country, Gender gender,String oAuthUid, OAuthCategory oAuthCategory) {
		return new OAuthRegisterRequest(userId, email, firstName, lastName, country, gender, oAuthUid, oAuthCategory);
	}
}