package com.example.kbuddy_backend.fixtures;

import com.example.kbuddy_backend.blog.entity.*;
import com.example.kbuddy_backend.user.entity.User;

import java.util.ArrayList;
import java.util.List;

public class BlogFixtures {
    
    public static Blog createBlog() {
        return Blog.builder()
                .writer(UserFixtures.createUser())
                .title("Test Blog Title")
                .content("Test Blog Content")
                .category(Category.OTHERS)
                .imageUrls(List.of("image1.jpg", "image2.jpg"))
                .build();
    }

    public static BlogComment createComment(Blog blog, User user) {
        return BlogComment.builder()
                .blog(blog)
                .writer(user)
                .content("Test Comment")
                .build();
    }

    public static BlogComment createReply(Blog blog, User user, BlogComment parent) {
        return BlogComment.builder()
                .blog(blog)
                .writer(user)
                .parent(parent)
                .content("Test Reply")
                .build();
    }

    public static BlogHeart createHeart(Blog blog, User user) {
        return new BlogHeart(user, blog);
    }

    public static BlogBookmark createBookmark(Blog blog, User user) {
        return new BlogBookmark(user, blog);
    }
} 