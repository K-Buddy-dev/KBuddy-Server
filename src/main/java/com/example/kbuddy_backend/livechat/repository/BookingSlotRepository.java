package com.example.kbuddy_backend.livechat.repository;

import com.example.kbuddy_backend.livechat.entity.BookingSlot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingSlotRepository extends JpaRepository<BookingSlot, Long> {

    List<BookingSlot> findByBookingId(Long bookingId);
}
