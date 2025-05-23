package com.example.kbuddy_backend.blog.repository;

import com.example.kbuddy_backend.blog.entity.BlogComment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.kbuddy_backend.blog.entity.QBlogComment.blogComment;
import com.example.kbuddy_backend.blog.entity.QBlogComment;
import com.example.kbuddy_backend.blog.entity.QBlogHeart;

@Repository
@RequiredArgsConstructor
public class BlogCommentRepositoryImpl implements BlogCommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<BlogComment> findCommentsWithWriterAndChildren(Long blogId) {
        QBlogComment child = new QBlogComment("child");

        return queryFactory
                .selectDistinct(blogComment)
                .from(blogComment)
                .leftJoin(blogComment.writer).fetchJoin()
                .leftJoin(blogComment.children, child).fetchJoin()
                .leftJoin(child.writer).fetchJoin()
                .where(blogComment.blog.id.eq(blogId)
                        .and(blogComment.parent.isNull()))
                .orderBy(
                    blogComment.createdDate.asc(),
                    child.createdDate.asc()
                )
                .fetch();
    }

    @Override
    public Map<Long, Long> findCommentHeartCounts(List<Long> commentIds) {
        QBlogHeart heart = new QBlogHeart("heart");
        
        return queryFactory
                .select(heart.blogComment.id, heart.count())
                .from(heart)
                .where(heart.blogComment.id.in(commentIds))
                .groupBy(heart.blogComment.id)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                    tuple -> tuple.get(heart.blogComment.id),
                    tuple -> tuple.get(heart.count())
                ));
    }

    @Override
    public Set<Long> findHeartedCommentIds(List<Long> commentIds, Long userId) {
        QBlogHeart heart = new QBlogHeart("heart");
        
        return queryFactory
                .select(heart.blogComment.id)
                .from(heart)
                .where(heart.blogComment.id.in(commentIds)
                        .and(heart.user.id.eq(userId)))
                .fetch()
                .stream()
                .collect(Collectors.toSet());
    }
} 