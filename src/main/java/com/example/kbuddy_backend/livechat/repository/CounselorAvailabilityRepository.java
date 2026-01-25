package com.example.kbuddy_backend.livechat.repository;

import com.example.kbuddy_backend.livechat.entity.CounselorAvailability;
import com.example.kbuddy_backend.livechat.entity.CounselorProfile;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CounselorAvailabilityRepository extends JpaRepository<CounselorAvailability, Long> {

    List<CounselorAvailability> findByCounselorAndAvailableDateBetween(
            CounselorProfile counselor, LocalDate startDate, LocalDate endDate);

    List<CounselorAvailability> findByCounselorIdAndAvailableDateBetween(
            Long counselorId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT ca FROM CounselorAvailability ca WHERE ca.counselor.id = :counselorId " +
            "AND ca.availableDate = :date AND ca.isBooked = false")
    List<CounselorAvailability> findAvailableSlots(
            @Param("counselorId") Long counselorId, @Param("date") LocalDate date);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT ca FROM CounselorAvailability ca WHERE ca.id = :id")
    Optional<CounselorAvailability> findByIdWithLock(@Param("id") Long id);

    boolean existsByCounselorAndAvailableDateAndStartTime(
            CounselorProfile counselor, LocalDate date, java.time.LocalTime startTime);
}
