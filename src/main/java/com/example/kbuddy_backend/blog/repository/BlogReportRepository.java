package com.example.kbuddy_backend.blog.repository;

import com.example.kbuddy_backend.blog.entity.BlogReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BlogReportRepository extends JpaRepository<BlogReport, Long> {
    boolean existsByBlogIdAndReporterId(Long blogId, Long reportedId);
    List<BlogReport> findAllByBlogId(Long blogId);

    @Query("""
            select br.blog.id as blogId,
                   br.blog.title as blogTitle,
                   br.blog.writer.id as writerId,
                   br.blog.writer.username as writerUsername,
                   br.blog.writer.email as writerEmail,
                   count(br.id) as reportCount
            from BlogReport br
            group by br.blog.id, br.blog.title, br.blog.writer.id, br.blog.writer.username, br.blog.writer.email
            order by count(br.id) desc
            """)
    List<BlogReportSummary> findReportSummaries();
}
