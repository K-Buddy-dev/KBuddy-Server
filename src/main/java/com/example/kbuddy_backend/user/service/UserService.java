package com.example.kbuddy_backend.user.service;

import com.example.kbuddy_backend.user.dto.request.UserBioRequest;
import com.example.kbuddy_backend.user.dto.response.UserAuthorityResponse;
import com.example.kbuddy_backend.user.dto.response.UserResponse;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getUser(User user) {
        User findUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));
        List<String> authorities = findUser.getAuthorities().stream()
                .map(authority -> authority.getAuthorityName().name())
                .toList();
        return UserResponse.of(findUser.getId(), findUser.getUsername(), findUser.getEmail(), authorities,
                findUser.getProfileImageUrl(), findUser.getBio(), findUser.getFirstName(), findUser.getLastName(),
                findUser.getCreatedDate(), findUser.getGender(), findUser.getCountry(),findUser.isActive());
    }

    @Transactional
    public void saveUserBio(UserBioRequest request, User user) {
        user.updateBio(request.bio());
    }
}
