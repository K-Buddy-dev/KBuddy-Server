package com.example.kbuddy_backend.livechat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "booking_slot",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_booking_slot_availability",
                columnNames = {"availability_id"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "availability_id", nullable = false)
    private CounselorAvailability availability;

    @Builder
    public BookingSlot(Booking booking, CounselorAvailability availability) {
        this.booking = booking;
        this.availability = availability;
    }
}
