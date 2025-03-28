package com.example.kbuddy_backend.blog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Table(name = "blog_bookmark")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_bookmark_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id")
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_collection_id")
    private BlogCollection blogCollection;

    public BlogBookmark(Blog blog, BlogCollection blogCollection) {
        this.blogCollection = blogCollection;
        this.blog = blog;
    }
} 