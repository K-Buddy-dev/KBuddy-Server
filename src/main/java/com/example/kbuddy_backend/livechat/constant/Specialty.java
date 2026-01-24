package com.example.kbuddy_backend.livechat.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Specialty {
    UNIVERSITY("대학 입학"),
    CAREER("취업/커리어"),
    VISA("비자/이민"),
    LIFE("생활/정착"),
    OTHER("기타");

    private final String description;
}
