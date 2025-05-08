package com.example.kbuddy_backend.qna.repository;

import static com.example.kbuddy_backend.qna.entity.QQna.qna;

import com.example.kbuddy_backend.qna.constant.SortBy;
import com.example.kbuddy_backend.qna.entity.Qna;
import com.example.kbuddy_backend.qna.constant.QnaStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QnaRepositoryImpl implements QnaRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Qna> paginationNoOffset(Long qnaId, String title, int pageSize, SortBy sortBy, Integer categoryCode, QnaStatus status) {
        return jpaQueryFactory.selectFrom(qna)
                .where(
                    ltQnaId(qnaId), 
                    titleOrDescriptionContains(title),
                    eqCategoryCode(categoryCode),
                    eqStatus(status)
                )
                .orderBy(getOrderSpecifier(sortBy))
                .limit(pageSize)
                .fetch();
    }

    private BooleanExpression ltQnaId(Long qnaId) {
        if (qnaId == null) {
            return null;
        }
        return qna.id.lt(qnaId);
    }
    
    private BooleanExpression titleOrDescriptionContains(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return qna.title.like("%" + keyword + "%").or(qna.description.like("%" + keyword + "%"));
    }
    
    private BooleanExpression eqCategoryCode(Integer categoryCode) {
        if (categoryCode == null) {
            return null;
        }
        return qna.categoryCode.eq(categoryCode);
    }

    private BooleanExpression eqStatus(QnaStatus status) {
        if (status == null) {
            return null;
        }
        return qna.status.eq(status);
    }

    private OrderSpecifier<?> getOrderSpecifier(SortBy sortBy) {
        if (sortBy == SortBy.VIEW_COUNT) {
            return qna.viewCount.desc();
        } else if (sortBy == SortBy.HEART_COUNT) {
            return qna.heartCount.desc();
        } else if (sortBy == SortBy.COMMENT_COUNT) {
            return qna.comments.size().desc();
        } else if (sortBy == SortBy.OLDEST){
            return qna.id.asc();
        } else { //Latest
            return qna.id.desc();
        }
    }
}