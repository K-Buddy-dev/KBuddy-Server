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

    /**
     * 로그인 사용자가 볼 수 있는 문의글: 공개글 + 본인이 작성했거나 본인이 상담사인 비밀글.
     * user 파라미터가 null 인 경우는 {@link #findPublicInquiries} 를 사용한다.
     */
    @Query("SELECT i FROM CounselorInquiry i WHERE i.counselor.id = :counselorId " +
            "AND (i.isSecret = false OR i.writer = :user OR i.counselor = :user) " +
            "ORDER BY i.createdDate DESC")
    Page<CounselorInquiry> findVisibleInquiries(
            @Param("counselorId") Long counselorId,
            @Param("user") User user,
            Pageable pageable);

    /**
     * 비로그인 사용자에게 보여줄 문의글: 공개글만.
     *
     * 파라미터를 NULL 비교에 쓰면 DB에 따라 타입 추론 문제가 생길 수 있어
     * 쿼리 자체를 분리한다.
     */
    @Query("SELECT i FROM CounselorInquiry i WHERE i.counselor.id = :counselorId " +
            "AND i.isSecret = false " +
            "ORDER BY i.createdDate DESC")
    Page<CounselorInquiry> findPublicInquiries(
            @Param("counselorId") Long counselorId,
            Pageable pageable);

    Page<CounselorInquiry> findByWriter(User writer, Pageable pageable);
}
