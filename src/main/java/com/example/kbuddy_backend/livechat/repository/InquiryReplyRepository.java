package com.example.kbuddy_backend.livechat.repository;

import com.example.kbuddy_backend.livechat.entity.InquiryReply;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface InquiryReplyRepository extends JpaRepository<InquiryReply, Long> {

    List<InquiryReply> findByInquiryIdOrderByCreatedAtAsc(Long inquiryId);

    long countByInquiryId(Long inquiryId);

    @Query("SELECT DISTINCT r.inquiry.id FROM InquiryReply r WHERE r.inquiry.id IN :inquiryIds")
    Set<Long> findInquiryIdsWithReplies(@Param("inquiryIds") List<Long> inquiryIds);
}
