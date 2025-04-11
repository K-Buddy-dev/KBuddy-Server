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
    public List<Blog> paginationNoOffset(Long blogId, String title, int pageSize, SortBy sortBy) {
        return paginationNoOffset(blogId, title, pageSize, sortBy, null);
    }

    @Override
    public List<Blog> paginationNoOffset(Long blogId, String title, int pageSize, SortBy sortBy, Integer categoryCode) {
        return jpaQueryFactory.selectFrom(blog)
                .where(
                        ltBlogId(blogId), 
                        titleOrDescriptionContains(title),
                        eqCategoryCode(categoryCode)
                )
                .orderBy(getOrderSpecifiers(sortBy)) // <-- 수정된 부분
                .limit(pageSize)
                .fetch();
    }


    private BooleanExpression ltBlogId(Long blogId) {
        if (blogId == null) {
            return null;
        }
        return blog.id.lt(blogId);
    }

    private BooleanExpression titleOrDescriptionContains(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return blog.title.like("%" + keyword + "%").or(blog.description.like("%" + keyword + "%"));
    }

    private BooleanExpression eqCategoryCode(Integer categoryCode) {
        if (categoryCode == null) {
            return null;
        }
        // blog 의 categoryCode 는 list 타입이기 때문에 eq -> contains 으로 변경
        return blog.categoryCode.contains(categoryCode);
    }

//    private BooleanExpression containsTitleOrDescription(String title) {
//        if (title == null || title.isEmpty()) {
//            return null;
//        }
//        return blog.title.like("%" + title + "%").or(blog.description.like("%" + title + "%"));
//    }

    private OrderSpecifier<?> getOrderSpecifiers(SortBy sortBy) {
//        switch (sortBy) {
//            case VIEW_COUNT:
//                return new OrderSpecifier[]{blog.viewCount.desc()};
//            case HEART_COUNT:
//                return new OrderSpecifier[]{blog.heartCount.desc()};
//            case COMMENT_COUNT:
//                return new OrderSpecifier[]{blog.comments.size().desc()};
//            case OLDEST:
//                return new OrderSpecifier[]{blog.id.asc()};
//            case CATEGORY_VIEW_COUNT:
//                return new OrderSpecifier[]{blog.category.category.asc(), blog.viewCount.desc()};
//            case CATEGORY_HEART_COUNT:
//                return new OrderSpecifier[]{blog.category.category.asc(), blog.heartCount.desc()};
//            case CATEGORY_COMMENT_COUNT:
//                return new OrderSpecifier[]{blog.category.category.asc(), blog.comments.size().desc()};
//            case CATEGORY_OLDEST:
//                return new OrderSpecifier[]{blog.category.category.asc(), blog.id.asc()};
//            case CATEGORY_LATEST:
//                return new OrderSpecifier[]{blog.category.category.asc(), blog.id.desc()};
//            default: // LATEST
//                return new OrderSpecifier[]{blog.id.desc()};
//        }

        if (sortBy == SortBy.VIEW_COUNT) {
            return blog.viewCount.desc();
        } else if (sortBy == SortBy.HEART_COUNT) {
            return blog.heartCount.desc();
        } else if (sortBy == SortBy.COMMENT_COUNT) {
            return blog.comments.size().desc();
        } else if (sortBy == SortBy.OLDEST) {
            return blog.id.asc();
        } else { // Latest
            return blog.id.desc();
        }
    }
} 