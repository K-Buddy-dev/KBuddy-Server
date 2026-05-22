package com.example.kbuddy_backend.livechat.repository;

import com.example.kbuddy_backend.livechat.entity.CounselorPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CounselorPhotoRepository extends JpaRepository<CounselorPhoto, Long> {
    List<CounselorPhoto> findByCounselorIdOrderBySortOrderAsc(Long counselorId);
    void deleteByCounselorId(Long counselorId);
}
