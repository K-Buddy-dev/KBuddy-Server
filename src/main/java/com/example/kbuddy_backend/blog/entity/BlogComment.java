package com.example.kbuddy_backend.blog.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import com.example.kbuddy_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "blog_comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id")
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private BlogComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<BlogComment> replies = new ArrayList<>();

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogCommentHeart> hearts = new ArrayList<>();

    private String content;
    private int heartCount;
    private boolean isReply;

    @Builder
    public BlogComment(Blog blog, User writer, BlogComment parent, String content) {
        this.blog = blog;
        this.writer = writer;
        this.parent = parent;
        this.content = content;
        this.isReply = parent != null;
        this.heartCount = 0;
    }

    public void plusHeart(BlogCommentHeart heart) {
        this.heartCount += 1;
        this.hearts.add(heart);
    }

    public void minusHeart(BlogCommentHeart heart) {
        if (this.heartCount > 0) {
            this.heartCount -= 1;
        }
        this.hearts.remove(heart);
    }

    public void addReply(BlogComment reply) {
        this.replies.add(reply);
    }
} 