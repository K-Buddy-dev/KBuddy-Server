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
@Table(name = "blog")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Blog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User writer;

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL)
    private List<BlogComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogHeart> hearts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Category category;

    private String title;
    private String content;
    private int heartCount;
    private int viewCount;
    private int reportCount;

    @ElementCollection
    @CollectionTable(
        name = "blog_images",
        joinColumns = @JoinColumn(name = "blog_id")
    )
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @Builder
    public Blog(User writer, String title, String content, Category category, List<String> imageUrls) {
        this.writer = writer;
        this.title = title;
        this.content = content;
        this.category = category;
        if (imageUrls != null) {
            this.imageUrls = imageUrls;
        }
    }

    public void update(String title, String content, Category category, List<String> imageUrls) {
        this.title = title;
        this.content = content;
        this.category = category;
        if (imageUrls != null) {
            this.imageUrls.clear();
            this.imageUrls.addAll(imageUrls);
        }
    }

    public void plusHeart(BlogHeart blogHeart) {
        this.heartCount += 1;
        this.hearts.add(blogHeart);
        blogHeart.setBlog(this);
    }

    public void minusHeart(BlogHeart blogHeart) {
        if (this.heartCount > 0) {
            this.heartCount -= 1;
        }
        this.hearts.remove(blogHeart);
    }

    public void plusViewCount() {
        this.viewCount += 1;
    }

    public void plusReportCount() {
        this.reportCount += 1;
    }

    public void addComment(BlogComment comment) {
        this.comments.add(comment);
        comment.setBlog(this);
    }
} 