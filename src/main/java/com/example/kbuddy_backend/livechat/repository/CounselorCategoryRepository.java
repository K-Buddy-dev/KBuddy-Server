package com.example.kbuddy_backend.livechat.repository;

import com.example.kbuddy_backend.livechat.entity.CounselorCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounselorCategoryRepository extends JpaRepository<CounselorCategory, Long> {
    void deleteByCounselorId(Long counselorId);
}
