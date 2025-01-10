package com.example.kbuddy_backend.fixtures;

import static com.example.kbuddy_backend.user.constant.UserRole.NORMAL_USER;

import com.example.kbuddy_backend.user.constant.OAuthCategory;
import com.example.kbuddy_backend.user.entity.Authority;
import com.example.kbuddy_backend.user.entity.User;

public class UserFixtures {
    public static final String EMAIL = "k-buddy@gmail.com";
    public static final String USERNAME = "k-buddy";
    public static final String PASSWORD = "1q2w3e";

    public static User createUser() {

        User user = User.builder()
                .email(EMAIL)
                .username(USERNAME)
                .password(PASSWORD)
                .build();
        user.addAuthority(new Authority(NORMAL_USER));
        return user;
    }

    public static User createOAuthUser() {

        User user = User.builder()
                .email(EMAIL)
                .username(USERNAME)
                .oAuthCategory(OAuthCategory.KAKAO)
                .build();
        user.addAuthority(new Authority(NORMAL_USER));
        return user;
    }
}
