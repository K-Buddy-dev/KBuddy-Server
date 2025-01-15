package com.example.kbuddy_backend.blog.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import com.example.kbuddy_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;


@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer categoryId;


    @Builder
    public Post(String title, String description, Integer categoryId, User user) {
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.user = user;
    }
}

