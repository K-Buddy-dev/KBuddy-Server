package com.example.kbuddy_backend.admin.config;

public class AdminAuthCookie {

    // 일반 사용자 쿠키 이름인 "refreshToken"과 구분
    public static final String ADMIN_TOKEN_NAME = "adminRefreshToken";
    // 관리자 쿠키는 관리자 API 요청에만 전달되도록 범위 제한, 해당 경로 아래의 요청에만 쿠키 전달
    public static final String ADMIN_TOKEN_PATH = "/kbuddy/v1/admin";

    private AdminAuthCookie() {
    }
}
