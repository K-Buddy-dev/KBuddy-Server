package com.example.kbuddy_backend.qna.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import com.example.kbuddy_backend.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QnaComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private User writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qna_id")
    private Qna qna;

    @OneToMany(mappedBy = "qnaComment",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<QnaHeart> qnaHearts = new ArrayList<>();

    //대댓글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private QnaComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QnaComment> children = new ArrayList<>();

    private int heartCount;

    public void setQna(Qna qna) {
        this.qna = qna;
    }

    @Builder
    public QnaComment(User writer, Qna qna, String content) {
        this.content = content;
        this.writer = writer;
        this.qna = qna;
    }

    public void plusHeart(QnaHeart qnaHeart) {
        //중복으로 좋아요 누르는 행위 방지
        if(!qnaHearts.contains(qnaHeart)){
            this.heartCount += 1;
            qnaHeart.setQnaComment(this);
            this.qnaHearts.add(qnaHeart);
        }

    }

    public void minusHeart(QnaHeart qnaHeart) {
        if (this.heartCount > 0) {
            this.heartCount -= 1;
        }
        qnaHeart.setQnaComment(null);
        this.qnaHearts.remove(qnaHeart);
    }

    public void addQna(Qna qna) {
        this.qna = qna;
        qna.addComment(this);
    }


}
