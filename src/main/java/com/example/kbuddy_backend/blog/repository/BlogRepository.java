package com.example.kbuddy_backend.blog.repository;

import com.example.kbuddy_backend.blog.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Blog, Long>, BlogRepositoryCustom {
    Page<Blog> findAllByOrderByIdDesc(Pageable pageable);
    //Todo: 검색기능 @Query 와 JPQL 사용
    // Like 사용하면 성능 저하 고려
    // 다섯글자 이상 검색 가능하게 해야 데이터 불러올때 시간이 덜 걸림
}