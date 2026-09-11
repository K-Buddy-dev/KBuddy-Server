package com.example.kbuddy_backend.common.config;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {

    /**
     * false인 경우 비로그인(익명) 요청에서 null이 주입된다.
     * 게스트도 조회 가능한 엔드포인트에만 사용한다.
     */
    boolean required() default true;
}
