package com.example.kbuddy_backend.qna.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import com.example.kbuddy_backend.common.exception.MaximumReplyDepthExceededException;
import com.example.kbuddy_backend.qna.dto.response.QnaCommentResponse;
import com.example.kbuddy_backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import org.hibernate.annotations.SQLRestriction;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("del_yn = false")
public class QnaComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private User writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qna_id")
    private Qna qna;

    @OneToMany(mappedBy = "qnaComment")
    private List<QnaHeart> qnaHearts = new ArrayList<>();

    //대댓글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private QnaComment parent;

    @OneToMany(mappedBy = "parent")
    private List<QnaComment> children = new ArrayList<>();

    private int heartCount;

    public void setQna(Qna qna) {
        this.qna = qna;
    }

    @Builder
    public QnaComment(Long id, String content, User writer, Qna qna) {
        this.id = id;
        this.content = content;
        this.writer = writer;
        this.qna = qna;
    }

    public void plusHeart(QnaHeart qnaHeart) {
        this.heartCount += 1;
        this.qnaHearts.add(qnaHeart);
    }

    public void minusHeart(QnaHeart qnaHeart) {
        if (this.heartCount > 0) {
            this.heartCount -= 1;
        }
        this.qnaHearts.remove(qnaHeart);
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void addQna(Qna qna) {
        this.qna = qna;
        qna.addComment(this);
    }

    public boolean isReply() {
        return parent != null;
    }

    public void addChild(QnaComment child) {
        if (isReply()) {
            throw new MaximumReplyDepthExceededException();
        }
        this.children.add(child);
        child.parent = this;
    }

}