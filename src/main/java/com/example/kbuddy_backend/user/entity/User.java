package com.example.kbuddy_backend.user.entity;

import com.example.kbuddy_backend.common.entity.BaseTimeEntity;
import com.example.kbuddy_backend.qna.entity.QnaHeart;
import com.example.kbuddy_backend.user.constant.Country;
import com.example.kbuddy_backend.user.constant.Gender;
import com.example.kbuddy_backend.user.constant.OAuthCategory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Country country;

    private String bio;
    private String profileImageUrl;
    private boolean isActive = true;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Authority> authorities = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Nullable
    private OAuthCategory oauthCategory;

    @OneToMany(mappedBy = "user")
    private List<QnaHeart> qnaHeart = new ArrayList<>();

    public void addAuthority(Authority authority) {
        authorities.add(authority);
        authority.setUser(this);
    }

    public void updateBio(String bio) {
        this.bio = bio;
    }

    public void resetPassword(String password) {
        this.password = password;
    }

    @Builder
    public User(String username, String password, String email, String firstName, String lastName, Gender gender,
                Country country,String bio,
                @Nullable OAuthCategory oAuthCategory) {
        this.gender = gender;
        this.country = country;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.bio = bio;
        this.oauthCategory = oAuthCategory;
    }
}
