package com.example.kbuddy_backend.user.repository;

import com.example.kbuddy_backend.user.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority,Long> {
}
