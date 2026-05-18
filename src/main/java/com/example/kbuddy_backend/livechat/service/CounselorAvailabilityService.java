package com.example.kbuddy_backend.livechat.service;

import com.example.kbuddy_backend.common.exception.DuplicateException;
import com.example.kbuddy_backend.livechat.constant.SlotStatus;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounselorAvailabilityService {

    private final CounselorAvailabilityRepository availabilityRepository;
    private final CounselorProfileRepository counselorProfileRepository;

    /**
     * 단일 슬롯 추가
     */
    @Transactional
    public CounselorAvailability addAvailability(User counselor, LocalDate date, LocalTime startTime) {
        CounselorProfile profile = getCounselorProfile(counselor);

        // 중복 체크
        if (availabilityRepository.existsByCounselorAndSlotDateAndSlotStartTime(profile, date, startTime)) {
            throw new DuplicateException("해당 시간대에 이미 가용 시간이 존재합니다");
        }

        CounselorAvailability availability = CounselorAvailability.builder()
                .counselor(profile)
                .slotDate(date)
                .slotStartTime(startTime)
                .build();

        return availabilityRepository.save(availability);
    }

    /**
     * 여러 슬롯 일괄 추가 (특정 날짜의 시작~종료 시간 범위)
     */
    @Transactional
    public List<CounselorAvailability> addAvailabilityBulk(User counselor, LocalDate date,
                                                            LocalTime startTime, LocalTime endTime) {
        validateBulkTimeRange(startTime, endTime);

        CounselorProfile profile = getCounselorProfile(counselor);

        List<CounselorAvailability> created = new ArrayList<>();
        LocalTime current = startTime;

        while (current.isBefore(endTime)) {
            if (!availabilityRepository.existsByCounselorAndSlotDateAndSlotStartTime(profile, date, current)) {
                CounselorAvailability availability = CounselorAvailability.builder()
                        .counselor(profile)
                        .slotDate(date)
                        .slotStartTime(current)
                        .build();
                created.add(availabilityRepository.save(availability));
            }
            current = current.plusMinutes(30);
        }

        return created;
    }

    /**
     * 슬롯 삭제 (AVAILABLE 상태만 가능)
     */
    @Transactional
    public void removeAvailability(User counselor, Long availabilityId) {
        CounselorAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new IllegalArgumentException("가용 시간을 찾을 수 없습니다"));

        // 본인 소유 확인
        if (!availability.getCounselor().getUser().getId().equals(counselor.getId())) {
            throw new IllegalArgumentException("이 가용 시간을 삭제할 권한이 없습니다");
        }

        // AVAILABLE 상태만 삭제 가능
        if (!availability.isAvailable()) {
            throw new IllegalStateException("예약 가능 상태의 슬롯만 삭제할 수 있습니다");
        }

        availabilityRepository.delete(availability);
    }

    /**
     * 슬롯 차단 (상담사가 수동으로 특정 시간 비활성화)
     */
    @Transactional
    public void blockSlot(User counselor, Long availabilityId) {
        CounselorAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new IllegalArgumentException("가용 시간을 찾을 수 없습니다"));

        if (!availability.getCounselor().getUser().getId().equals(counselor.getId())) {
            throw new IllegalArgumentException("이 슬롯을 차단할 권한이 없습니다");
        }

        availability.block();
    }

    /**
     * 슬롯 차단 해제
     */
    @Transactional
    public void unblockSlot(User counselor, Long availabilityId) {
        CounselorAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new IllegalArgumentException("가용 시간을 찾을 수 없습니다"));

        if (!availability.getCounselor().getUser().getId().equals(counselor.getId())) {
            throw new IllegalArgumentException("이 슬롯을 해제할 권한이 없습니다");
        }

        availability.unblock();
    }

    /**
     * 특정 날짜의 가용 슬롯 조회
     */
    public List<CounselorAvailability> getAvailableSlots(Long counselorId, LocalDate date) {
        return availabilityRepository.findSlotsByStatus(counselorId, date, SlotStatus.AVAILABLE);
    }

    private CounselorProfile getCounselorProfile(User counselor) {
        return counselorProfileRepository.findByUserId(counselor.getId())
                .orElseThrow(() -> new IllegalArgumentException("상담사로 등록되지 않은 사용자입니다"));
    }

    private void validateBulkTimeRange(LocalTime startTime, LocalTime endTime) {
        validateSlotTime(startTime);
        validateSlotTime(endTime);

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 이전이어야 합니다");
        }
    }

    private void validateSlotTime(LocalTime time) {
        if (time.getMinute() % 30 != 0 || time.getSecond() != 0) {
            throw new IllegalArgumentException("슬롯 시간은 30분 단위여야 합니다 (예: 09:00, 09:30)");
        }
    }
}
