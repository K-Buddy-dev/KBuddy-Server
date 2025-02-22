package com.example.kbuddy_backend.user.util;

import com.example.kbuddy_backend.user.dto.request.OAuthRegisterRequest;
import com.example.kbuddy_backend.user.dto.request.RegisterRequest;
import com.example.kbuddy_backend.user.entity.User;

public class UserConverter {

    public static User fromRegisterRequest(RegisterRequest request, String password) {
        return User.builder()
                .username(request.userId())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .country(request.country())
                .gender(request.gender())
                .birthDate(request.birthDate())
                .email(request.email())
                .password(password)
                .build();
    }

    public static User fromOAuthRegisterRequest(OAuthRegisterRequest request) {
        return User.builder()
                .username(request.userId())
                .oAuthCategory(request.oAuthCategory())
                .oAuthUid(request.oAuthUid())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .country(request.country())
                .gender(request.gender())
                .birthDate(request.birthDate())
                .email(request.email())
                .build();
    }
}
