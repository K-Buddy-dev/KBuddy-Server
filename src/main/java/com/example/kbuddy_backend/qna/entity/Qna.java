package com.example.kbuddy_backend.qna.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import com.example.kbuddy_backend.qna.constant.QnaStatus;
import com.example.kbuddy_backend.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jdk.jfr.Category;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("del_yn = false")
public class Qna extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User writer;

    @OneToMany(mappedBy = "qna", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QnaHeart> qnaHearts = new ArrayList<>();

    @OneToMany(mappedBy = "qna", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QnaComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "qna", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QnaImage> imageUrls = new ArrayList<>();

    private String hashtag;

    private int categoryCode;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;

    private int heartCount;
    private int viewCount;
    private int reportCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QnaStatus status = QnaStatus.DRAFT;

    @Builder
    public Qna(User writer, String title, String description, String hashtag, int category, QnaStatus status) {
        this.writer = writer;
        this.title = title;
        this.hashtag = hashtag;
        this.categoryCode = category;
        this.description = description;
        this.status = (status != null) ? status : QnaStatus.DRAFT;
    }

    public void update(String title, String description, String hashtag, Integer category, QnaStatus status) {

        if (title != null && !title.isEmpty()) {
            this.title = title;
        }
        if (description != null && !description.isEmpty()) {
            this.description = description;
        }

        if (hashtag != null && !hashtag.isEmpty()) {
            this.hashtag = hashtag;
        }
      
        if (category != null) {
            this.categoryCode = category;
        }

        if (status != null) {
            this.status = status;
        }
    }

    public void addImage(QnaImage qnaImage) {
        qnaImage.setQna(this);
        imageUrls.add(qnaImage);
    }

    public void deleteImage(Long imageId) {
        imageUrls.removeIf(image -> image.getId().equals(imageId));
    }

    public void addComment(QnaComment qnaComment) {
        comments.add(qnaComment);
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

    public void plusViewCount() {
        this.viewCount += 1;
    }

    public int getCommentCount() {
        return comments.size();
    }

    public void setStatus(QnaStatus status) {
        if (status != null) {
            this.status = status;
        }
    }

    public void plusReportCount() {
        this.reportCount += 1;
    }
}