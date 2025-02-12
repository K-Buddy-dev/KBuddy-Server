package com.example.kbuddy_backend.blog.entity;

import com.example.kbuddy_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "blog_comment_heart")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogCommentHeart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "heart_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private BlogComment comment;

    public BlogCommentHeart(User user, BlogComment comment) {
        this.user = user;
        this.comment = comment;
    }
} 