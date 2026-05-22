package com.example.kbuddy_backend.livechat.repository;

import com.example.kbuddy_backend.livechat.entity.CounselorPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CounselorPromotionRepository extends JpaRepository<CounselorPromotion, Long> {
    Optional<CounselorPromotion> findByCounselorId(Long counselorId);
    void deleteByCounselorId(Long counselorId);
}
