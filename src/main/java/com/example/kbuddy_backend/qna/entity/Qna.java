package com.example.kbuddy_backend.qna.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
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
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Qna extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User writer;

    @OneToMany(mappedBy = "qna")
    private List<QnaHeart> qnaHearts = new ArrayList<>();

    @OneToMany(mappedBy = "qna")
    private List<QnaComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "qna")
    private List<QnaCategory> category = new ArrayList<>();

    private String hashtag;

    private String title;
    private String description;

    private int heartCount;

    private int viewCount;

    @Builder
    public Qna(User writer, String title, String description,String hashtag) {
        this.writer = writer;
        this.title = title;
        this.hashtag = hashtag;
        this.description = description;

    }

    public void addComment(QnaComment qnaComment) {
        comments.add(qnaComment);
    }

    public void plusHeart(QnaHeart qnaHeart) {
        this.heartCount += 1;
        this.qnaHearts.add(qnaHeart);
    }

    public void minusHeart(QnaHeart qnaHeart) {
        if (this.heartCount > 0){
            this.heartCount -= 1;
        }
        this.qnaHearts.remove(qnaHeart);
    }

    public void plusViewCount() {
        this.viewCount += 1;
    }

    public int getCommentCount() {
        return comments.size();
    }


}
