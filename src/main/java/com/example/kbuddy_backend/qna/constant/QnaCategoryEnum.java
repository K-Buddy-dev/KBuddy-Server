package com.example.kbuddy_backend.qna.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum QnaCategoryEnum {

    VISA("visa"),
    KOREAN("korean");

    private final String category;
}
