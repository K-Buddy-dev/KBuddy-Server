package com.example.kbuddy_backend.chat.constant;

public enum ChatRole {
    COUNSELOR("상담자"),
    CLIENT("내담자");

    private final String description;

    ChatRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
