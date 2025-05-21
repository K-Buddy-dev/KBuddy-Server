package com.example.kbuddy_backend.qna.repository;

import com.example.kbuddy_backend.qna.entity.QnaComment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.kbuddy_backend.qna.entity.QQnaComment.qnaComment;
import com.example.kbuddy_backend.qna.entity.QQnaComment;
import com.example.kbuddy_backend.qna.entity.QQnaHeart;

@Repository
@RequiredArgsConstructor
public class QnaCommentRepositoryImpl implements QnaCommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<QnaComment> findCommentsWithWriterAndChildren(Long qnaId) {
        QQnaComment child = new QQnaComment("child");

        return queryFactory
                .selectDistinct(qnaComment)
                .from(qnaComment)
                .leftJoin(qnaComment.writer).fetchJoin()
                .leftJoin(qnaComment.children, child).fetchJoin()
                .leftJoin(child.writer).fetchJoin()
                .where(qnaComment.qna.id.eq(qnaId)
                        .and(qnaComment.parent.isNull()))
                .orderBy(
                    qnaComment.createdDate.asc(),
                    child.createdDate.asc()
                )
                .fetch();
    }

    @Override
    public Map<Long, Long> findCommentHeartCounts(List<Long> commentIds) {
        QQnaHeart heart = new QQnaHeart("heart");
        
        return queryFactory
                .select(heart.qnaComment.id, heart.count())
                .from(heart)
                .where(heart.qnaComment.id.in(commentIds))
                .groupBy(heart.qnaComment.id)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                    tuple -> tuple.get(heart.qnaComment.id),
                    tuple -> tuple.get(heart.count())
                ));
    }

    @Override
    public Set<Long> findHeartedCommentIds(List<Long> commentIds, Long userId) {
        QQnaHeart heart = new QQnaHeart("heart");
        
        return queryFactory
                .select(heart.qnaComment.id)
                .from(heart)
                .where(heart.qnaComment.id.in(commentIds)
                        .and(heart.user.id.eq(userId)))
                .fetch()
                .stream()
                .collect(Collectors.toSet());
    }
}