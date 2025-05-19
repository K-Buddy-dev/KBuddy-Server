package com.example.kbuddy_backend.qna.repository;

import com.example.kbuddy_backend.qna.entity.QnaComment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.kbuddy_backend.qna.entity.QQnaComment.qnaComment;

@Repository
@RequiredArgsConstructor
public class QnaCommentRepositoryImpl implements QnaCommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<QnaComment> findCommentsWithWriterAndChildren(Long qnaId) {
        return queryFactory
                .selectDistinct(qnaComment)
                .from(qnaComment)
                .leftJoin(qnaComment.writer).fetchJoin()
                .leftJoin(qnaComment.children).fetchJoin()
                .leftJoin(qnaComment.children.any().writer).fetchJoin()
                .where(qnaComment.qna.id.eq(qnaId)
                        .and(qnaComment.parent.isNull()))
                .orderBy(qnaComment.createdDate.desc())
                .fetch();
    }
}