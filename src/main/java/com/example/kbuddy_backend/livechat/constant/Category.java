package com.example.kbuddy_backend.livechat.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    RESTAURANT("Restaurant"),
    CAFE_DESSERT("Cafe/Dessert"),
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
}
