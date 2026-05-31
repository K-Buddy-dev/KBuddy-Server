package com.example.kbuddy_backend.announcement.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
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
@Table(name = "announcement")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("del_yn = false")
public class Announcement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "announcement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User writer;

    @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnnouncementImage> imageUrls = new ArrayList<>();

    private String title;
    private String description;

    private int viewCount;

    @Builder
    public Announcement(User writer, String title, String description){
        this.writer = writer;
        this.title = title;
        this.description = description;
    }

    public void update(String title, String description){
        if(title != null && !title.isEmpty()){
            this.title = title;
        }
        if(description != null && !description.isEmpty()){
            this.description = description;
        }
    }

    public void addImage(AnnouncementImage announcementImage){
        announcementImage.setAnnouncement(this);
        imageUrls.add(announcementImage);
    }

    public void deleteImage(Long imageId) {
        imageUrls.removeIf(image -> image.getId().equals(imageId));
    }

    public void plusViewCount() {
        this.viewCount += 1;
    }
}
