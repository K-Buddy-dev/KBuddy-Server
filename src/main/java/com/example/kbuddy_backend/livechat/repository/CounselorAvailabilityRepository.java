package com.example.kbuddy_backend.livechat.repository;

import com.example.kbuddy_backend.livechat.constant.SlotStatus;
import com.example.kbuddy_backend.livechat.entity.CounselorAvailability;
import com.example.kbuddy_backend.livechat.entity.CounselorProfile;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CounselorAvailabilityRepository extends JpaRepository<CounselorAvailability, Long> {

    List<CounselorAvailability> findByCounselorAndSlotStartUtcBetween(
            CounselorProfile counselor, LocalDateTime startUtc, LocalDateTime endUtc);

    List<CounselorAvailability> findByCounselorIdAndSlotStartUtcBetween(
            Long counselorId, LocalDateTime startUtc, LocalDateTime endUtc);

    @Query("SELECT ca FROM CounselorAvailability ca WHERE ca.counselor.id = :counselorId " +
            "AND ca.slotStartUtc BETWEEN :startUtc AND :endUtc AND ca.status = :status")
    List<CounselorAvailability> findSlotsByStatus(
            @Param("counselorId") Long counselorId,
            @Param("startUtc") LocalDateTime startUtc,
            @Param("endUtc") LocalDateTime endUtc,
            @Param("status") SlotStatus status);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT ca FROM CounselorAvailability ca WHERE ca.id = :id")
    Optional<CounselorAvailability> findByIdWithLock(@Param("id") Long id);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT ca FROM CounselorAvailability ca WHERE ca.id IN :ids")
    List<CounselorAvailability> findAllByIdWithLock(@Param("ids") List<Long> ids);

    boolean existsByCounselorAndSlotStartUtc(CounselorProfile counselor, LocalDateTime slotStartUtc);

    @Modifying
    @Query("DELETE FROM CounselorAvailability ca WHERE ca.counselor.id = :counselorId AND ca.status = :status")
    void deleteByCounselorIdAndStatus(@Param("counselorId") Long counselorId, @Param("status") SlotStatus status);
}
