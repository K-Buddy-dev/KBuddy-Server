package com.example.kbuddy_backend.common.advice.response;

import lombok.Getter;

/**
 * K-Buddy Custom Error Code ex) KB-HTTP-001 -> K-Buddy API 서버(KB)에서 보낸 HTTP 유형의 response가 001번 - BAD_REQUEST를 리턴함.
 */
@Getter
public enum CustomCode {

    HTTP_200("KB-HTTP-200", "요청을 성공적으로 수행함."),
    HTTP_201("KB-HTTP-201", "요청을 수행하였고 요소가 성공적으로 생성이 됨."),
    HTTP_204("KB-HTTP-204", "요청이 정상적으로 수행 되었으며, 불러온 리소스가 없음."),
    HTTP_400("KB-HTTP-001", "요청이 잘못 됨."),
    HTTP_401("KB-AUTH-001", "Auth 헤더가 없음."),
    HTTP_403("KB-AUTH-002", "Auth 헤더에 포함된 토큰의 유저가 적절한 권한이 없음."),
    HTTP_404("KB-HTTP-002", "요청할 Endpoint가 존재하지 않음."),
    HTTP_405("KB-HTTP-003", "요청 메서드가 잘못 됨."),
    HTTP_406("KB-HTTP-004", "해당 Endpoint를 요청할 수 없음."),
    HTTP_408("KB-HTTP-005", "해당 Endpoint 요청에 너무 오랜 시간이 걸림."),
    HTTP_409("KB-DATA-006", "데이터 중복으로 인한 충돌이 발생함."),
    HTTP_422("KB-HTTP-006", "해당 Endpoint 요청 작업을 수행할 수 없음."),
    HTTP_429("KB-HTTP-007", "해당 Endpoint에 너무 많은 접근이 수행되고 있음."),
    HTTP_500("KB-SEVR-001", "서버에 문제가 발생함."),
    HTTP_502("KB-SEVR-002", "서버 네트워크에 문제가 발생함."),
    HTTP_503("KB-SEVR-003", "서버를 이용할 수 없음."),
    HTTP_504("KB-SEVR-004", "인터넷 게이트웨이가 서버 호출에 너무 오랜 시간이 걸림.");

    private final String code;
    private final String message;

    CustomCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}