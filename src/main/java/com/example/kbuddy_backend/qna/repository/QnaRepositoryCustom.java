package com.example.kbuddy_backend.qna.repository;


import com.example.kbuddy_backend.qna.constant.SortBy;
import com.example.kbuddy_backend.qna.entity.Qna;
import java.util.List;

public interface QnaRepositoryCustom {

    List<Qna> paginationNoOffset(Long qnaId, String title, int pageSize, SortBy sortBy);

}