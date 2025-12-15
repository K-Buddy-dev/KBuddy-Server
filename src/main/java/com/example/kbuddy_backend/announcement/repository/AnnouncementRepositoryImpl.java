package com.example.kbuddy_backend.announcement.repository;

import static com.example.kbuddy_backend.announcement.entity.QAnnouncement.announcement;

import com.example.kbuddy_backend.announcement.constant.SortBy;
import com.example.kbuddy_backend.announcement.entity.Announcement;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class AnnouncementRepositoryImpl implements AnnouncementRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Announcement> paginationNoOffset(Long announcementId, String title, int pageSize, SortBy sortBy) {
        return jpaQueryFactory.selectFrom(announcement)
                .where(
                        ltAnnouncementId(announcementId),
                        titleOrDescriptionContains(title)
                )
                .orderBy(getOrderSpecifier(sortBy))
                .limit(pageSize)
                .fetch();
    }

    private BooleanExpression ltAnnouncementId(Long announcementId) {
        if(announcementId == null) {
            return null;
        }
        return announcement.id.lt(announcementId);
    }

    private BooleanExpression titleOrDescriptionContains(String keyword) {
        if(keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return announcement.title.like("%" + keyword + "%").or(announcement.description.like("%" + keyword + "%"));
    }

    private OrderSpecifier<?> getOrderSpecifier(SortBy sortBy) {
        return announcement.id.desc();
    }
}
