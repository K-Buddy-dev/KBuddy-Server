package com.example.kbuddy_backend.blog.entity;

import static lombok.AccessLevel.PROTECTED;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import com.example.kbuddy_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="blog_post")
@NoArgsConstructor(access = PROTECTED)
public class BlogPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User writer;

    @Builder
    public BlogPost(String title, String content, User writer) {
        this.title = title;
        this.content = content;
        this.writer = writer;
    }
}
