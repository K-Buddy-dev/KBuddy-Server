package com.example.kbuddy_backend.user.dto.response;

import com.example.kbuddy_backend.user.constant.Country;
import com.example.kbuddy_backend.user.constant.Gender;
import com.example.kbuddy_backend.user.entity.Authority;
import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(Long id, String userId, String email, List<String> roles,String profileImageUrl, String bio, String firstName,
                           String lastName,
                           Gender gender, Country country, boolean isActive,LocalDateTime createdDate) {

    public static UserResponse of(Long id, String userId, String email, List<String> roles, String profileImageUrl, String bio,
                                  String firstName, String lastName, LocalDateTime createdDate, Gender gender,
                                  Country country,boolean isActive) {
        return new UserResponse(id, userId, email,roles, profileImageUrl, bio, firstName, lastName, gender, country,isActive,
                createdDate);
    }
}
