package com.example.kbuddy_backend.livechat.repository;

import com.example.kbuddy_backend.livechat.entity.InquiryReply;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryReplyRepository extends JpaRepository<InquiryReply, Long> {

    List<InquiryReply> findByInquiryIdOrderByCreatedAtAsc(Long inquiryId);

    long countByInquiryId(Long inquiryId);
}
