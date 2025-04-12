package com.example.kbuddy_backend.blog.repository;

import com.example.kbuddy_backend.blog.constant.SortBy;
import com.example.kbuddy_backend.blog.entity.Blog;
import java.util.List;

public interface BlogRepositoryCustom {
    List<Blog> paginationNoOffset(Long blogId, String title, int pageSize, SortBy sortBy);

    List<Blog> paginationNoOffset(Long blogId, String title, int pageSize, SortBy sortBy, Integer categoryCode);
} 