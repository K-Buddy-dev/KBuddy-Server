package com.example.kbuddy_backend.livechat.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    VISA_IMMIGRATION("Visa"),
    KOREAN_LANGUAGE("Korean"),
    HOUSING("Housing"),
    BANKING_FINANCE("Banking & Finance"),
    MOBILE_INTERNET("Mobile & Internet"),
    HEALTHCARE("Healthcare"),
    EDUCATION("Education"),
    JOB_CAREER("Job & Career"),
    DAILY_LIFE("Daily Life"),
    TRANSPORTATION("Transportation"),
    SHOPPING_LOCAL("Shopping & Local"),
    LEGAL_ADMIN("Legal & Admin"),
    TRAVEL_LOCAL_GUIDE("Travel & Local Guide"),
    RELATIONSHIP_CULTURE("Relationship & Culture"),
    EMERGENCY_HELP("Emergency Help"),
    OTHERS("Others");

    private final String displayName;
}
