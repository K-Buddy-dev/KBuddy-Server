package com.example.kbuddy_backend.livechat.repository;

import com.example.kbuddy_backend.livechat.constant.SlotStatus;
import com.example.kbuddy_backend.livechat.entity.CounselorAvailability;
import com.example.kbuddy_backend.livechat.entity.CounselorProfile;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CounselorAvailabilityRepository extends JpaRepository<CounselorAvailability, Long> {

    List<CounselorAvailability> findByCounselorAndSlotDateBetween(
            CounselorProfile counselor, LocalDate startDate, LocalDate endDate);

    List<CounselorAvailability> findByCounselorIdAndSlotDateBetween(
            Long counselorId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT ca FROM CounselorAvailability ca WHERE ca.counselor.id = :counselorId " +
            "AND ca.slotDate = :date AND ca.status = :status")
    List<CounselorAvailability> findSlotsByStatus(
            @Param("counselorId") Long counselorId,
            @Param("date") LocalDate date,
            @Param("status") SlotStatus status);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT ca FROM CounselorAvailability ca WHERE ca.id = :id")
    Optional<CounselorAvailability> findByIdWithLock(@Param("id") Long id);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT ca FROM CounselorAvailability ca WHERE ca.id IN :ids")
    List<CounselorAvailability> findAllByIdWithLock(@Param("ids") List<Long> ids);

    boolean existsByCounselorAndSlotDateAndSlotStartTime(
            CounselorProfile counselor, LocalDate slotDate, LocalTime slotStartTime);
}
