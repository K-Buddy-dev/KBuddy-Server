package com.example.kbuddy_backend.user.repository;

import com.example.kbuddy_backend.user.constant.OAuthCategory;
import com.example.kbuddy_backend.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndOauthCategory(String email, OAuthCategory oAuthCategory);
}
