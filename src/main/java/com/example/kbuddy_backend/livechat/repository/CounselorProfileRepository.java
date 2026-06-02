package com.example.kbuddy_backend.livechat.repository;

import com.example.kbuddy_backend.livechat.entity.CounselorProfile;
import com.example.kbuddy_backend.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CounselorProfileRepository extends JpaRepository<CounselorProfile, Long> {

    Optional<CounselorProfile> findByUser(User user);

    Optional<CounselorProfile> findByUserId(Long userId);

    Optional<CounselorProfile> findByUuid(UUID uuid);

    boolean existsByUserId(Long userId);

    // del_yn 필터 무시 — 소프트딜리트된 행도 포함해 중복 체크
    @Query(value = "SELECT EXISTS(SELECT 1 FROM counselor_profile WHERE user_id = :userId)", nativeQuery = true)
    boolean existsByUserIdIgnoreDeleted(@Param("userId") Long userId);

    @Query("SELECT cp FROM CounselorProfile cp ORDER BY cp.ratingAvg DESC")
    Page<CounselorProfile> findAllOrderByRatingDesc(Pageable pageable);
}
