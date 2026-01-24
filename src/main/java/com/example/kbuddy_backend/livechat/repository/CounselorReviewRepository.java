package com.example.kbuddy_backend.livechat.repository;

import com.example.kbuddy_backend.livechat.entity.CounselorReview;
import com.example.kbuddy_backend.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CounselorReviewRepository extends JpaRepository<CounselorReview, Long> {

    Page<CounselorReview> findByCounselor(User counselor, Pageable pageable);

    Page<CounselorReview> findByCounselorId(Long counselorId, Pageable pageable);

    List<CounselorReview> findTop3ByCounselorIdOrderByCreatedAtDesc(Long counselorId);

    Optional<CounselorReview> findByBookingId(Long bookingId);

    boolean existsByBookingId(Long bookingId);

    @Query("SELECT AVG(r.rating) FROM CounselorReview r WHERE r.counselor.id = :counselorId")
    BigDecimal calculateAverageRating(@Param("counselorId") Long counselorId);

    @Query("SELECT COUNT(r) FROM CounselorReview r WHERE r.counselor.id = :counselorId")
    long countByCounselorId(@Param("counselorId") Long counselorId);
}
