package com.example.kbuddy_backend.blog.repository;

import static com.example.kbuddy_backend.blog.entity.QBlog.blog;

import com.example.kbuddy_backend.blog.constant.SortBy;
import com.example.kbuddy_backend.blog.entity.Blog;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BlogRepositoryImpl implements BlogRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Blog> paginationNoOffset(Long blogId, String keyword, int pageSize, SortBy sortBy) {
        return jpaQueryFactory.selectFrom(blog)
                .where(ltBlogId(blogId), 
                       blog.title.like("%" + keyword + "%")
                           .or(blog.content.like("%" + keyword + "%")))
                .orderBy(getOrderSpecifier(sortBy))
                .limit(pageSize)
                .fetch();
    }

    private BooleanExpression ltBlogId(Long blogId) {
        if (blogId == null) {
            return null;
        }
        return blog.id.lt(blogId);
    }

    private OrderSpecifier<?> getOrderSpecifier(SortBy sortBy) {
        if (sortBy == SortBy.VIEW_COUNT) {
            return blog.viewCount.desc();
        } else if (sortBy == SortBy.HEART_COUNT) {
            return blog.heartCount.desc();
        } else if (sortBy == SortBy.COMMENT_COUNT) {
            return blog.comments.size().desc();
        } else {
            return blog.id.desc();
        }
    }
} 