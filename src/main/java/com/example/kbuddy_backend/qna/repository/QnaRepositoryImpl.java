package com.example.kbuddy_backend.qna.repository;

import static com.example.kbuddy_backend.qna.entity.QQna.qna;

import com.example.kbuddy_backend.qna.constant.SortBy;
import com.example.kbuddy_backend.qna.entity.Qna;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QnaRepositoryImpl implements QnaRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Qna> paginationNoOffset(Long qnaId, String title, int pageSize, SortBy sortBy) {
        return jpaQueryFactory.selectFrom(qna)
                .where(ltQnaId(qnaId), qna.title.like("%"+title + "%").or(qna.description.like("%" + title + "%")))
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

    private OrderSpecifier<?> getOrderSpecifier(SortBy sortBy) {
        if (sortBy == SortBy.VIEW_COUNT) {
            return qna.viewCount.desc();
        } else if (sortBy == SortBy.HEART_COUNT) {
            return qna.heartCount.desc();
        } else if (sortBy == SortBy.COMMENT_COUNT) {
            return qna.comments.size().desc();
        } else {
            return qna.id.desc();
        }
    }
}