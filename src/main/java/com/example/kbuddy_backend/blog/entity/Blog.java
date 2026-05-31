package com.example.kbuddy_backend.blog.entity;

import com.example.kbuddy_backend.blog.constant.BlogStatus;
import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import com.example.kbuddy_backend.qna.constant.QnaStatus;
import com.example.kbuddy_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "blog")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("del_yn = false")
public class Blog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User writer;

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogHeart> blogHearts = new ArrayList<>();

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogImage> imageUrls = new ArrayList<>();

    private String hashtag;

    @ElementCollection
    @CollectionTable(
            name = "blog_categories", // 생성될 테이블 이름
            joinColumns = @JoinColumn(name = "blog_id") // 외래 키 칼럼 이름
    )
    @Column(name = "category_code") // 카테고리 코드가 저장될 칼럼 이름
    private List<Integer> categoryCode = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlogStatus status = BlogStatus.DRAFT;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;

    private int heartCount;
    private int viewCount;
    private int reportCount;

    @Builder
    public Blog(User writer, String title, String description, String hashtag, List<Integer> category,BlogStatus status) {
        this.writer = writer;
        this.title = title;
        this.description = description;
        this.hashtag = hashtag;
        this.categoryCode = category;
        this.status = (status != null) ? status : BlogStatus.DRAFT;
    }

    public void update(String title, String description, String hashtag, List<Integer> category, BlogStatus status) {
        if (title != null && !title.isEmpty()) {
            this.title = title;
        }
        if (description != null && !description.isEmpty()) {
            this.description = description;
        }
        if (hashtag != null && !hashtag.isEmpty()) {
            this.hashtag = hashtag;
        }
        if (category != null) {
            this.categoryCode = category;
        }

        if (status != null) {
            this.status = status;
        }
    }

    public void addImage(BlogImage blogImage){
        blogImage.setBlog(this);
        imageUrls.add(blogImage);
    }

    public void deleteImage(Long imageId){
        imageUrls.removeIf(image -> image.getId().equals(imageId));
    }

    public void addComment(BlogComment blogComment){comments.add(blogComment);}

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

    public void plusViewCount() {
        this.viewCount += 1;
    }

    public int getCommentCount() {
        return comments.size();
    }

    public void setStatus(BlogStatus status) {
        if (status != null) {
            this.status = status;
        }
    }

    public void plusReportCount() {
        this.reportCount += 1;
    }
}