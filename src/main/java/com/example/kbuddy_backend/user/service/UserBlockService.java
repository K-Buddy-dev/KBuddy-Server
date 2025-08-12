package com.example.kbuddy_backend.user.service;

import com.example.kbuddy_backend.user.dto.request.UserBlockRequest;
import com.example.kbuddy_backend.user.dto.response.UserBlockResponse;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.entity.UserBlock;
import com.example.kbuddy_backend.user.exception.UserBlockException;
import com.example.kbuddy_backend.user.exception.UserNotFoundException;
import com.example.kbuddy_backend.user.repository.UserBlockRepository;
import com.example.kbuddy_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserBlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;

    // 사용자 차단
    @Transactional
    public UserBlockResponse blockUser(User blocker, UserBlockRequest request) {
        // 차단할 사용자 조회
        User blockedUser = userRepository.findByUuid(UUID.fromString(request.blockedUserId()))
                .orElseThrow(UserNotFoundException::new);

        // 자기 자신을 차단하려는 경우
        if (blocker.getId().equals(blockedUser.getId())) {
            throw new UserBlockException("자기 자신을 차단할 수 없습니다.");
        }

        // 이미 차단한 사용자인지 확인
        if (userBlockRepository.existsByBlockerAndBlocked(blocker, blockedUser)) {
            throw new UserBlockException("이미 차단한 사용자입니다.");
        }

        // 차단 관계 생성
        UserBlock userBlock = new UserBlock(blocker, blockedUser);
        UserBlock savedBlock = userBlockRepository.save(userBlock);

        return UserBlockResponse.of(
                savedBlock.getId(),
                savedBlock.getBlocker().getUuid().toString(),
                savedBlock.getBlocker().getUsername(),
                savedBlock.getBlocked().getUuid().toString(),
                savedBlock.getBlocked().getUsername(),
                savedBlock.getCreatedDate()
        );
    }

    // 사용자 차단 해제
    @Transactional
    public void unblockUser(User blocker, String blockedUserId) {
        User blockedUser = userRepository.findByUuid(UUID.fromString(blockedUserId))
                .orElseThrow(UserNotFoundException::new);

        // 차단 관계가 존재하는지 확인
        if (!userBlockRepository.existsByBlockerAndBlocked(blocker, blockedUser)) {
            throw new UserBlockException("차단하지 않은 사용자입니다.");
        }

        userBlockRepository.deleteByBlockerAndBlocked(blocker, blockedUser);
    }

    // 차단한 사용자 목록 조회
    public List<UserBlockResponse> getBlockedUsers(User blocker) {
        List<UserBlock> blockedUsers = userBlockRepository.findByBlockerOrderByCreatedDateDesc(blocker);
        
        return blockedUsers.stream()
                .map(block -> UserBlockResponse.of(
                        block.getId(),
                        block.getBlocker().getUuid().toString(),
                        block.getBlocker().getUsername(),
                        block.getBlocked().getUuid().toString(),
                        block.getBlocked().getUsername(),
                        block.getCreatedDate()
                ))
                .collect(Collectors.toList());
    }

    // 차단당한 사용자 목록 조회
    public List<UserBlockResponse> getBlockedByUsers(User blocked) {
        List<UserBlock> blockedByUsers = userBlockRepository.findByBlockedOrderByCreatedDateDesc(blocked);
        
        return blockedByUsers.stream()
                .map(block -> UserBlockResponse.of(
                        block.getId(),
                        block.getBlocker().getUuid().toString(),
                        block.getBlocker().getUsername(),
                        block.getBlocked().getUuid().toString(),
                        block.getBlocked().getUsername(),
                        block.getCreatedDate()
                ))
                .collect(Collectors.toList());
    }

    // 차단 관계 확인
    public boolean isBlocked(User blocker, User blocked) {
        return userBlockRepository.existsByBlockerAndBlocked(blocker, blocked);
    }

    // 사용자가 차단한 사용자 ID 목록 조회
    public List<Long> getBlockedUserIds(User blocker) {
        return userBlockRepository.findBlockedUserIdsByBlocker(blocker);
    }

    // 사용자를 차단한 사용자 ID 목록 조회
    public List<Long> getBlockerUserIds(User blocked) {
        return userBlockRepository.findBlockerUserIdsByBlocked(blocked);
    }
} 