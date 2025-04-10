package com.example.kbuddy_backend.qna.entity;

import com.example.kbuddy_backend.user.entity.User;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "qna_bookmark")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QnaBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_boomark_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="qna_id")
    private Qna qna;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public QnaBookmark(Qna qna, User user) {
        this.qna = qna;
        this.user = user;
    }
}