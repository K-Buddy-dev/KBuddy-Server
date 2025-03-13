package com.example.kbuddy_backend.blog.entity;

public enum Category {
    RESTAURANT_CAFE("Restaurant cafe/Dessert"),
    SHOPPING("Shopping"),
    ATTRACTION("Attraction"),
    LODGING("Lodging"),
    NATURE("Nature"),
    ART("Art"),
    BEAUTY_SPA("Beauty/Spa"),
    TRANSPORTATION("Transportation"),
    HEALTH("Health"),
    DAILY_LIFE("Daily Life"),
    OTHERS("Others");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 