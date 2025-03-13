package com.example.kbuddy_backend.blog.entity;

import com.example.kbuddy_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "blog_heart")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogHeart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "heart_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id")
    private Blog blog;

    public BlogHeart(User user, Blog blog) {
        this.user = user;
        this.blog = blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }
}