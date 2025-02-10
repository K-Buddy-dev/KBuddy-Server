package com.example.kbuddy_backend.common.dto;

import com.example.kbuddy_backend.common.constant.ImageFileType;

/**
 * type : 확장자 (PNG,JPEG)
 * name : 클라우드에 저장되는 object name
 * url : public 이미지 url
 * */

public record ImageFileDto(ImageFileType type, String name, String url) {
    public static ImageFileDto of(ImageFileType type, String name, String url) {
        return new ImageFileDto(type, name, url);
    }
}