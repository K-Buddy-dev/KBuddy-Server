package com.example.kbuddy_backend.announcement.controller;

import com.example.kbuddy_backend.announcement.constant.SortBy;
import com.example.kbuddy_backend.announcement.dto.request.AnnouncementSaveRequest;
import com.example.kbuddy_backend.announcement.dto.request.AnnouncementUpdateRequest;
import com.example.kbuddy_backend.announcement.dto.response.AllAnnouncementResponse;
import com.example.kbuddy_backend.announcement.dto.response.AnnouncementResponse;
import com.example.kbuddy_backend.announcement.service.AnnouncementService;
import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.user.dto.response.DefaultResponse;
import com.example.kbuddy_backend.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("kbuddy/v1/announcement")
@Tag(name = "Announcement API", description = "공지사항 API 목록")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "공지사항 게시글 작성", description = "공지사항 게시글을 작성합니다. qnaSaveRequest는 JSON 형식의 문자열로, 이미지는 선택적으로 multipart form data로 전송합니다.")
    public ResponseEntity<DefaultResponse> saveAnnouncement(
            @Valid @RequestPart(value = "announcementSaveRequest") AnnouncementSaveRequest announcementSaveRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @Parameter(hidden = true) @CurrentUser User user) {
        announcementService.saveAnnouncement(announcementSaveRequest, images, user);
        return ResponseEntity.noContent().build(); // 204 No Content 반환
    }

    //전체 조회(페이징)
    @GetMapping
    @Operation(summary = "공지사항 게시글 전체 조회", description = "공지사항 게시글을 최신순으로 전체 조회 합니다.")
    public ResponseEntity<AllAnnouncementResponse> getAllAnouncement(@RequestParam(value = "size") int pageSize,
                                                                     @RequestParam(value = "id", required = false) Long announcementId,
                                                                     @RequestParam(value = "keyword", defaultValue = "") String title,
                                                                     @RequestParam(required = false, value = "sort") SortBy sortBy,
                                                                     @Parameter(hidden = true) @CurrentUser User user) {
        AllAnnouncementResponse allAnnouncementResponse = announcementService.getAllAnnouncement(pageSize, announcementId, title, sortBy, user);
        return ResponseEntity.ok().body(allAnnouncementResponse);
    }

    @GetMapping("/{announcementId}")
    @Operation(summary = "특정 공지사항 게시글 조회", description = "공지사항 게시글 id를 통해 조회합니다.")
    public ResponseEntity<AnnouncementResponse> getAnnouncement(
            @PathVariable Long announcementId,
            @Parameter(hidden = true) @CurrentUser User user) {
        AnnouncementResponse announcement = announcementService.getAnnouncement(announcementId, user);
        return ResponseEntity.ok().body(announcement);
    }

    @PatchMapping(value = "/{announcementId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "공지사항 게시글 업데이트", description = "공지사항 게시글을 업데이트 합니다. 이미지 추가/삭제 등을 포함합니다.")
    public ResponseEntity<AnnouncementResponse> updateAnouncement(
            @PathVariable final Long announcementId,
            @Valid @RequestPart(value = "announcementUpadate") AnnouncementUpdateRequest announcementUpdateRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> newFiles,
            @Parameter(hidden = true) @CurrentUser User user) {
        AnnouncementResponse announcement = announcementService.updateAnnouncement(announcementId, announcementUpdateRequest, newFiles, user);
        return ResponseEntity.ok().body(announcement);
    }

    @DeleteMapping("/{announcementId}")
    @Operation(summary = "공지사항 게시글 삭제", description = "공지사항 게시글을 삭제합니다.")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable final Long announcementId, @Parameter(hidden = true) @CurrentUser User user) {
        announcementService.deleteAnnouncement(announcementId, user);
        return ResponseEntity.noContent().build();
    }
}
