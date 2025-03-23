package com.example.kbuddy_backend.blog.entity;

import com.example.kbuddy_backend.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_collection_id")
    private int id;

    private String collectionName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "blogCollection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogBookmark> blogBookmark = new ArrayList<>();

    public void addBookmark(BlogBookmark blogBookmark) {this.blogBookmark.add(blogBookmark);}

    public void removeBookmark(BlogBookmark blogBookmark) {this.blogBookmark.remove(blogBookmark);}

    public void updateCollectionName(String collectionName) {this.collectionName = collectionName;}

    public BlogCollection(String collectionName, User user) {
        this.collectionName = collectionName;
        this.user = user;
    }
}
