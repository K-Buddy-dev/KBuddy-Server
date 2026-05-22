package com.example.kbuddy_backend.livechat.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "counselor_photo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounselorPhoto extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private CounselorProfile counselor;

    @Column(nullable = false)
    private String photoUrl;

    @Column(nullable = false)
    private Integer sortOrder;

    @Builder
    public CounselorPhoto(CounselorProfile counselor, String photoUrl, Integer sortOrder) {
        this.counselor = counselor;
        this.photoUrl = photoUrl;
        this.sortOrder = sortOrder;
    }
}
