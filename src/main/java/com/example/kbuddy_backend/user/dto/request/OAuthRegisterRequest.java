package com.example.kbuddy_backend.user.dto.request;

import com.example.kbuddy_backend.user.constant.Country;
import com.example.kbuddy_backend.user.constant.Gender;
import com.example.kbuddy_backend.user.constant.OAuthCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

//todo: validation
public record OAuthRegisterRequest(@Pattern(regexp = "^[a-zA-Z0-9]{3,20}$",message = "닉네임 유효성 검사 실패") String userId, @Email(message = "이메일 형식에 맞지 않습니다.") String email, String firstName, String lastName,
								   Country country, Gender gender, String oauthUid, OAuthCategory oAuthCategory) {
	public static OAuthRegisterRequest of(String userId, String email, String firstName, String lastName,
                                          Country country, Gender gender,String oauthUid, OAuthCategory oAuthCategory) {
		return new OAuthRegisterRequest(userId, email, firstName, lastName, country, gender, oauthUid, oAuthCategory);
	}
}