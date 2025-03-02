package com.example.kbuddy_backend.blog.repository;

import com.example.kbuddy_backend.blog.entity.BlogReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogReportRepository extends JpaRepository<BlogReport, Long> {
    boolean existsByBlogIdAndReporterId(Long blogId, Long reportedId);
    List<BlogReport> findAllByBlogId(Long blogId);
}
