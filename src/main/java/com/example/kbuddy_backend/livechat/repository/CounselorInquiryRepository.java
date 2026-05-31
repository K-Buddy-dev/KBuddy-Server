package com.example.kbuddy_backend.livechat.repository;

import com.example.kbuddy_backend.livechat.entity.CounselorInquiry;
import com.example.kbuddy_backend.user.entity.User;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CounselorInquiryRepository extends JpaRepository<CounselorInquiry, Long> {

    Page<CounselorInquiry> findByCounselorId(Long counselorId, Pageable pageable);

    List<CounselorInquiry> findTop5ByCounselorIdAndIsSecretFalseOrderByCreatedDateDesc(Long counselorId);

    @Query("SELECT i FROM CounselorInquiry i WHERE i.counselor.id = :counselorId " +
            "AND (i.isSecret = false OR i.writer = :user OR i.counselor = :user)")
    Page<CounselorInquiry> findVisibleInquiries(
            @Param("counselorId") Long counselorId,
            @Param("user") User user,
            Pageable pageable);

    Page<CounselorInquiry> findByWriter(User writer, Pageable pageable);
}
