package com.example.kbuddy_backend.livechat.service;

import com.example.kbuddy_backend.livechat.entity.CounselorAvailability;
import com.example.kbuddy_backend.livechat.entity.CounselorProfile;
import com.example.kbuddy_backend.livechat.repository.CounselorAvailabilityRepository;
import com.example.kbuddy_backend.livechat.repository.CounselorProfileRepository;
import com.example.kbuddy_backend.user.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounselorAvailabilityService {

    private final CounselorAvailabilityRepository availabilityRepository;
    private final CounselorProfileRepository counselorProfileRepository;

    @Transactional
    public void addAvailability(User counselor, LocalDate date, LocalTime startTime) {
        CounselorProfile profile = counselorProfileRepository.findByUserId(counselor.getId())
                .orElseThrow(() -> new IllegalArgumentException("상담사로 등록되지 않은 사용자입니다"));

        // 중복 체크
        if (availabilityRepository.existsByCounselorAndAvailableDateAndStartTime(profile, date, startTime)) {
            throw new IllegalStateException("해당 시간대에 이미 가용 시간이 존재합니다");
        }

        CounselorAvailability availability = CounselorAvailability.builder()
                .counselor(profile)
                .availableDate(date)
                .startTime(startTime)
                .build();

        availabilityRepository.save(availability);
    }

    @Transactional
    public void removeAvailability(User counselor, Long availabilityId) {
        CounselorAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new IllegalArgumentException("가용 시간을 찾을 수 없습니다"));

        // 본인 소유 확인
        if (!availability.getCounselor().getUser().getId().equals(counselor.getId())) {
            throw new IllegalArgumentException("이 가용 시간을 삭제할 권한이 없습니다");
        }

        // 이미 예약된 슬롯은 삭제 불가
        if (availability.isBooked()) {
            throw new IllegalStateException("예약된 슬롯은 삭제할 수 없습니다");
        }

        availabilityRepository.delete(availability);
    }
}
