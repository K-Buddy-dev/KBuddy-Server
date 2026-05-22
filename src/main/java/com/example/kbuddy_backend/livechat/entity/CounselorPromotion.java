package com.example.kbuddy_backend.livechat.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "counselor_promotion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounselorPromotion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false, unique = true)
    private CounselorProfile counselor;

    @Column(nullable = false)
    private Integer promotionalPrice;

    @Column(nullable = false)
    private Integer promotionSessionMinutes;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Builder
    public CounselorPromotion(CounselorProfile counselor, Integer promotionalPrice,
                               Integer promotionSessionMinutes, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일보다 이후여야 합니다");
        }
        this.counselor = counselor;
        this.promotionalPrice = promotionalPrice;
        this.promotionSessionMinutes = promotionSessionMinutes;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }
}
