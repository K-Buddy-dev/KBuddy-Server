package com.example.kbuddy_backend.livechat.repository;

import com.example.kbuddy_backend.livechat.entity.CounselorProfile;
import com.example.kbuddy_backend.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CounselorProfileRepository extends JpaRepository<CounselorProfile, Long> {

    Optional<CounselorProfile> findByUser(User user);

    Optional<CounselorProfile> findByUserId(Long userId);

    Optional<CounselorProfile> findByUuid(UUID uuid);

    boolean existsByUserId(Long userId);

    @Query("SELECT cp FROM CounselorProfile cp ORDER BY cp.ratingAvg DESC")
    Page<CounselorProfile> findAllOrderByRatingDesc(Pageable pageable);
}
