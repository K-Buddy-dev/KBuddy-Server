package com.example.kbuddy_backend.common.validate;

import com.example.kbuddy_backend.common.exception.InvalidDateFormatException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateValidator {
    public static void isValidDate(String date) {
        try {
            if (date == null || date.isEmpty()) {
                return;
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
            LocalDate.parse(date, formatter);
        } catch (Exception e) {
            throw new InvalidDateFormatException();
        }
    }
}
