package com.example.kbuddy_backend.livechat.constant;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CategoryConverter implements AttributeConverter<Category, String> {

    @Override
    public String convertToDatabaseColumn(Category category) {
        return category == null ? null : category.name();
    }

    @Override
    public Category convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Category.valueOf(value);
        } catch (IllegalArgumentException e) {
            return Category.OTHERS;
        }
    }
}
