package com.example.kbuddy_backend.blog.repository;

import com.example.kbuddy_backend.blog.entity.BlogComment;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BlogCommentRepositoryCustom {

    // List<BlogComment> findAllCommentsByBlogId(Long blogId);

    List<BlogComment> findCommentsWithWriterAndChildren(Long blogId);
    Map<Long, Long> findCommentHeartCounts(List<Long> commentIds);
    Set<Long> findHeartedCommentIds(List<Long> commentIds, Long userId);
}
