package com.example.kbuddy_backend.livechat.entity;

import com.example.kbuddy_backend.livechat.constant.Category;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "counselor_category",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_counselor_category",
                columnNames = {"counselor_id", "category"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounselorCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private CounselorProfile counselor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Builder
    public CounselorCategory(CounselorProfile counselor, Category category) {
        this.counselor = counselor;
        this.category = category;
    }
}
