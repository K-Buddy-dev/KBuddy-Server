package com.example.kbuddy_backend.blog.entity;

import com.example.kbuddy_backend.blog.exception.MaximumReplyDepthExceededException;
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

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private User writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id")
    private Blog blog;

    @OneToMany(mappedBy = "blogComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogHeart> hearts = new ArrayList<>();

    //대댓글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private BlogComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogComment> children = new ArrayList<>();

    @Builder
    public BlogComment(String content, Blog blog, User writer, BlogComment parent) {
        this.content = content;
        this.blog = blog;
        this.writer = writer;
        this.parent = parent;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void plusHeart(BlogHeart heart) {
        this.hearts.add(heart);
    }

    public void minusHeart(BlogHeart heart) {
        this.hearts.remove(heart);
    }

    public int getHeartCount() {
        return this.hearts.size();
    }

    public void addBlog(Blog blog) {
        this.blog = blog;
        blog.addComment(this);
    }

    public void addChild(BlogComment child) {
        if (isReply()) {
            throw new MaximumReplyDepthExceededException();
        }
        this.children.add(child);
        child.parent = this;
    }

    public boolean isReply() {
        return this.parent != null;
    }
}