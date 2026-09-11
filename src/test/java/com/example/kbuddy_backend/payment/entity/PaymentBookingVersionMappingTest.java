package com.example.kbuddy_backend.payment.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.kbuddy_backend.livechat.entity.Booking;

import jakarta.persistence.Column;
import jakarta.persistence.Version;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class PaymentBookingVersionMappingTest {

    @Test
    void paymentHasNonNullableLongVersion() throws NoSuchFieldException {
        assertVersionMapping(Payment.class);
    }

    @Test
    void bookingHasNonNullableLongVersion() throws NoSuchFieldException {
        assertVersionMapping(Booking.class);
    }

    private void assertVersionMapping(Class<?> entityType) throws NoSuchFieldException {
        Field versionField = entityType.getDeclaredField("version");

        assertEquals(Long.class, versionField.getType());
        assertNotNull(versionField.getAnnotation(Version.class));

        Column column = versionField.getAnnotation(Column.class);
        assertNotNull(column);
        assertFalse(column.nullable());
    }
}
