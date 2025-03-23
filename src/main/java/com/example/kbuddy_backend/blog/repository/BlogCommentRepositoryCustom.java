package com.example.kbuddy_backend.blog.repository;

import com.example.kbuddy_backend.blog.entity.BlogComment;
import java.util.List;

public interface BlogCommentRepositoryCustom {

    List<BlogComment> findAllCommentsByBlogId(Long blogId);
}
