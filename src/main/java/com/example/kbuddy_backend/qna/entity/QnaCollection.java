package com.example.kbuddy_backend.qna.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QnaCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_collection_id")
    private Long id;

    private String collectionName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "qnaCollection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QnaBookmark> qnaBookmark = new ArrayList<>();

    public void addBookmark(QnaBookmark qnaBookmark) {
        this.qnaBookmark.add(qnaBookmark);
    }

    public void removeBookmark(QnaBookmark qnaBookmark) {
        this.qnaBookmark.remove(qnaBookmark);
    }

    public void updateCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public QnaCollection(String collectionName, User user) {
        this.collectionName = collectionName;
        this.user = user;
    }
}