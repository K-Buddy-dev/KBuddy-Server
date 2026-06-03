package com.example.kbuddy_backend.blog.constant;

public final class BlogCategories {
    public static final int RESTAURANT = 0;
    public static final int CAFE_DESSERT = 1;
    public static final int SHOPPING = 2;
    public static final int ATTRACTION = 3;
    public static final int LODGING = 4;
    public static final int NATURE = 5;
    public static final int ART = 6;
    public static final int BEAUTY_SPA = 7;
    public static final int TRANSPORTATION = 8;
    public static final int HEALTH = 9;
    public static final int DAILY_LIFE = 10;
    public static final int OTHERS = 11;

    private BlogCategories() {}

    public static String getCategoryName(int code) {
        return switch (code) {
            case RESTAURANT -> "Restaurant";
            case CAFE_DESSERT -> "Cafe/Dessert";
            case SHOPPING -> "Shopping";
            case ATTRACTION -> "Attraction";
            case LODGING -> "Lodging";
            case NATURE -> "Nature";
            case ART -> "Art";
            case BEAUTY_SPA -> "Beauty/Spa";
            case TRANSPORTATION -> "Transportation";
            case HEALTH -> "Health";
            case DAILY_LIFE -> "Daily Life";
            case OTHERS -> "Others";
            default -> "Unknown";
        };
    }
}
