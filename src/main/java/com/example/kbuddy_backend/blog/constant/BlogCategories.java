package com.example.kbuddy_backend.blog.constant;

public final class BlogCategories {

    // GENERAL categories (0~11)
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

    // BUDDY categories (0~5)
    public static final int BUDDY_LANGUAGE_EXCHANGE = 0;
    public static final int BUDDY_LOCAL_FRIENDS = 1;
    public static final int BUDDY_HOBBY = 2;
    public static final int BUDDY_TRAVEL = 3;
    public static final int BUDDY_CAFE_MEETUP = 4;
    public static final int BUDDY_KOREAN_PRACTICE = 5;

    private static final int GENERAL_MAX = 11;
    private static final int BUDDY_MAX = 5;

    private BlogCategories() {}

    public static String getCategoryName(int code, BlogType type) {
        if (type == BlogType.BUDDY) {
            return switch (code) {
                case BUDDY_LANGUAGE_EXCHANGE -> "Language Exchange";
                case BUDDY_LOCAL_FRIENDS -> "Local Friends";
                case BUDDY_HOBBY -> "Hobby Buddy";
                case BUDDY_TRAVEL -> "Travel Buddy";
                case BUDDY_CAFE_MEETUP -> "Cafe / Meetup";
                case BUDDY_KOREAN_PRACTICE -> "Korean Practice";
                default -> "Unknown";
            };
        }
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

    public static boolean isValidCategoryCode(int code, BlogType type) {
        if (type == BlogType.BUDDY) {
            return code >= 0 && code <= BUDDY_MAX;
        }
        return code >= 0 && code <= GENERAL_MAX;
    }
}
