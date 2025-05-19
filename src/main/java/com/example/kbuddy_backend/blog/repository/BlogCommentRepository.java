package com.example.kbuddy_backend.blog.repository;

import com.example.kbuddy_backend.blog.entity.BlogComment;
import com.example.kbuddy_backend.qna.entity.QnaComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {
    Optional<BlogComment> findByIdAndWriterId(Long commentId, Long userId);

    List<BlogComment> findAllByBlogIdOrderByCreatedDateAsc(Long blogId);

    List<BlogComment> findByBlogIdAndParentIsNullOrderByCreatedDateDesc(Long blogId);

    List<BlogComment> findByParentIdOrderByCreatedDateDesc(Long parentId);
} 