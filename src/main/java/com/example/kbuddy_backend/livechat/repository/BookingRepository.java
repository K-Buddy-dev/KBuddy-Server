package com.example.kbuddy_backend.livechat.repository;

import com.example.kbuddy_backend.livechat.constant.BookingStatus;
import com.example.kbuddy_backend.livechat.entity.Booking;
import com.example.kbuddy_backend.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByCustomer(User customer, Pageable pageable);

    Page<Booking> findByCounselor(User counselor, Pageable pageable);

    Page<Booking> findByCustomerAndStatus(User customer, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.createdDate < :expireTime")
    List<Booking> findExpiredPendingBookings(
            @Param("status") BookingStatus status,
            @Param("expireTime") LocalDateTime expireTime);
}
