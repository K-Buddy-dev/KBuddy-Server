package com.example.kbuddy_backend.user.repository;

import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.entity.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {
    
    // 특정 사용자가 차단한 사용자 목록 조회
    List<UserBlock> findByBlockerOrderByCreatedDateDesc(User blocker);
    
    // 특정 사용자를 차단한 사용자 목록 조회
    List<UserBlock> findByBlockedOrderByCreatedDateDesc(User blocked);
    
    // 차단 관계 존재 여부 확인
    boolean existsByBlockerAndBlocked(User blocker, User blocked);
    
    // 차단 관계 조회
    Optional<UserBlock> findByBlockerAndBlocked(User blocker, User blocked);
    
    // 사용자가 차단한 사용자 ID 목록 조회
    @Query("SELECT ub.blocked.id FROM UserBlock ub WHERE ub.blocker = :blocker")
    List<Long> findBlockedUserIdsByBlocker(@Param("blocker") User blocker);
    
    // 사용자를 차단한 사용자 ID 목록 조회
    @Query("SELECT ub.blocker.id FROM UserBlock ub WHERE ub.blocked = :blocked")
    List<Long> findBlockerUserIdsByBlocked(@Param("blocked") User blocked);
    
    // 차단 관계 삭제
    void deleteByBlockerAndBlocked(User blocker, User blocked);
    
    // 사용자가 차단한 모든 관계 삭제
    void deleteByBlocker(User blocker);
    
    // 사용자를 차단한 모든 관계 삭제
    void deleteByBlocked(User blocked);
} 