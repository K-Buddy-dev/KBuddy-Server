package com.example.kbuddy_backend.blog.constant;

// 블로그 카테고리 코드를 정의하는 상수 클래스
public final class BlogCategories {
    // 카테고리 코드 상수 정의
    public static final int GENERAL = 1;    // 일반
    public static final int TECHNICAL = 2;  // 기술
    public static final int CAREER = 3;     // 경력/취업
    public static final int LIFESTYLE = 4;  // 라이프스타일
    public static final int STUDY = 5;      // 스터디
    public static final int EVENTS = 6;     // 이벤트
    public static final int OTHER = 99;     // 기타

    private BlogCategories() {
        // 인스턴스화 방지
    }

    /**
     * 카테고리 코드에 해당하는 카테고리명을 반환합니다.
     * @param code 카테고리 코드
     * @return 카테고리명
     */
    public static String getCategoryName(int code) {
        return switch (code) {
            case GENERAL -> "일반";
            case TECHNICAL -> "기술";
            case CAREER -> "경력/취업";
            case LIFESTYLE -> "라이프스타일";
            case STUDY -> "스터디";
            case EVENTS -> "이벤트";
            case OTHER -> "기타";
            default -> "알 수 없음";
        };
    }
}
