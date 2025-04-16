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

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private User writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id")
    private Blog blog;

    @OneToMany(mappedBy = "blogComment")
    private List<BlogHeart> blogHearts = new ArrayList<>();

    //대댓글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private BlogComment parent;

    @OneToMany(mappedBy = "parent")
    private List<BlogComment> children = new ArrayList<>();

    private int heartCount;

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    @Builder
    public BlogComment(User writer, Blog blog, String content) {
        this.content = content;
        this.writer = writer;
        this.blog = blog;
    }

    public void plusHeart(BlogHeart blogHeart) {
        this.heartCount += 1;
        this.blogHearts.add(blogHeart);
    }

    public void minusHeart(BlogHeart blogHeart) {
        if (this.heartCount > 0) {
            this.heartCount -= 1;
        }
        this.blogHearts.remove(blogHeart);
    }

    public void updateContent(String content) { this.content = content; }

    public void addBlog(Blog blog) {
        this.blog = blog;
        blog.addComment(this);
    }
}