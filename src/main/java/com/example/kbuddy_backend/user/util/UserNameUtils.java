package com.example.kbuddy_backend.user.util;

import com.example.kbuddy_backend.user.entity.User;

public final class UserNameUtils {

    private UserNameUtils() {}

    public static String fullName(User user) {
        String first = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String last = user.getLastName() != null ? user.getLastName().trim() : "";
        String name = (first + " " + last).trim();
        return name.isEmpty() ? user.getUsername() : name;
    }
}
