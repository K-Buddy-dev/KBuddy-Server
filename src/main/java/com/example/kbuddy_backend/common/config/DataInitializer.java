package com.example.kbuddy_backend.common.config;

import static com.example.kbuddy_backend.user.constant.UserRole.NORMAL_USER;

import com.example.kbuddy_backend.auth.dto.response.AccessTokenAndRefreshTokenResponse;
import com.example.kbuddy_backend.auth.service.AuthService;
import com.example.kbuddy_backend.blog.entity.Blog;
import com.example.kbuddy_backend.blog.entity.BlogBookmark;
import com.example.kbuddy_backend.blog.entity.BlogComment;
import com.example.kbuddy_backend.blog.entity.BlogHeart;
import com.example.kbuddy_backend.blog.entity.Category;
import com.example.kbuddy_backend.blog.repository.BlogBookmarkRepository;
import com.example.kbuddy_backend.blog.repository.BlogCommentRepository;
import com.example.kbuddy_backend.blog.repository.BlogHeartRepository;
import com.example.kbuddy_backend.blog.repository.BlogRepository;
import com.example.kbuddy_backend.qna.entity.Qna;
import com.example.kbuddy_backend.qna.repository.QnaRepository;
import com.example.kbuddy_backend.user.constant.Country;
import com.example.kbuddy_backend.user.constant.Gender;
import com.example.kbuddy_backend.user.entity.Authority;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final QnaRepository qnaRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final BlogRepository blogRepository;
    private final BlogCommentRepository blogCommentRepository;
    private final BlogHeartRepository blogHeartRepository;
    private final BlogBookmarkRepository blogBookmarkRepository;

    @PostConstruct
    public void initData() {
        // 더미 사용자 생성
        final String password = passwordEncoder.encode("kbuddy");
        User user = User.builder()
                .email("kbuddy@gmail.com")
                .username("johnhuh")
                .password(password)
                .firstName("tony")
                .lastName("stark")
                .gender(Gender.M)
                .bio("hello, i'm tony stark.")
                .country(Country.KR)
                .birthDate("000724")
                .build();
        user.addAuthority(new Authority(NORMAL_USER));
        User saveUser = userRepository.save(user);

        List<GrantedAuthority> grantedAuthorities = saveUser.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName().name()))
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(saveUser.getEmail(), saveUser.getPassword(), grantedAuthorities);
        AccessTokenAndRefreshTokenResponse token = authService.createToken(authenticationToken);
        log.info("테스트용 토큰 입니다.: {}", token.accessToken());

        // 더미 Qna 생성
        Qna qna = Qna.builder()
                .title("Dummy Title")
                .description("Dummy Description")
                .writer(user)
                .build();
        qnaRepository.save(qna);

        // 더미 Blog 생성
        Blog blog = Blog.builder()
                .title("블로그 제목")
                .content("블로그 글")
                .writer(user)
                .build();
        blogRepository.save(blog);

        // Create additional test users
        User user2 = createAndSaveUser("peter@gmail.com", "peter", "Peter", "Parker");
        User user3 = createAndSaveUser("steve@gmail.com", "steve", "Steve", "Rogers");
        
        // Create 10 dummy blogs with various interactions
        createDummyBlog("Korean Food Guide", "Best Korean restaurants in Seoul", Category.ART, user,
                List.of("food1.jpg", "food2.jpg"), user2, user3);
        createDummyBlog("Travel Tips", "How to travel in Korea", Category.ATTRACTION, user2,
                List.of("travel1.jpg"), user, user3);
        createDummyBlog("Language Exchange", "Looking for language exchange partner", Category.LODGING, user3,
                List.of("lang1.jpg", "lang2.jpg"), user, user2);
        createDummyBlog("Cultural Experience", "Traditional Korean culture", Category.HEALTH, user,
                List.of("culture1.jpg"), user2, user3);
        createDummyBlog("Study Tips", "How to study Korean effectively", Category.TRANSPORTATION, user2,
                List.of("study1.jpg"), user, user3);
        createDummyBlog("Housing Guide", "Finding accommodation in Seoul", Category.OTHERS, user3, 
                List.of("house1.jpg"), user, user2);
        createDummyBlog("Transportation Guide", "Using public transportation", Category.DAILY_LIFE, user,
                List.of("transport1.jpg"), user2, user3);
        createDummyBlog("Job Search Tips", "Finding jobs in Korea", Category.RESTAURANT_CAFE, user2,
                List.of("job1.jpg"), user, user3);
        createDummyBlog("Weekend Activities", "Fun things to do in Seoul", Category.BEAUTY_SPA, user3,
                List.of("weekend1.jpg"), user, user2);
        createDummyBlog("Restaurant Reviews", "Must-try restaurants", Category.NATURE, user,
                List.of("restaurant1.jpg"), user2, user3);
    }

    private User createAndSaveUser(String email, String username, String firstName, String lastName) {
        User user = User.builder()
                .email(email)
                .username(username)
                .password(passwordEncoder.encode("kbuddy"))
                .firstName(firstName)
                .lastName(lastName)
                .gender(Gender.M)
                .bio("Hello, I'm " + firstName)
                .country(Country.KR)
                .birthDate("000101")
                .build();
        user.addAuthority(new Authority(NORMAL_USER));
        return userRepository.save(user);
    }

    private void createDummyBlog(String title, String content, Category category, User writer,
                                 List<String> imageUrls, User commenter1, User commenter2) {
        Blog blog = Blog.builder()
                .title(title)
                .content(content)
                .category(category)
                .writer(writer)
                .imageUrls(imageUrls)
                .build();
        Blog savedBlog = blogRepository.save(blog);

        // Add comments
        BlogComment comment1 = createAndSaveComment(savedBlog, commenter1, "Great post! Very helpful.");
        BlogComment comment2 = createAndSaveComment(savedBlog, commenter2, "Thanks for sharing!");
        
        // Add replies to comments
        createAndSaveReply(savedBlog, writer, comment1, "Thank you for your feedback!");
        createAndSaveReply(savedBlog, commenter2, comment1, "I agree, very informative.");
        
        // Add hearts (likes)
        createAndSaveHeart(savedBlog, commenter1);
        createAndSaveHeart(savedBlog, commenter2);
        
        // Add bookmarks
        createAndSaveBookmark(savedBlog, commenter1);
    }

    private BlogComment createAndSaveComment(Blog blog, User writer, String content) {
        BlogComment comment = BlogComment.builder()
                .blog(blog)
                .writer(writer)
                .content(content)
                .build();
        return blogCommentRepository.save(comment);
    }

    private BlogComment createAndSaveReply(Blog blog, User writer, BlogComment parent, String content) {
        BlogComment reply = BlogComment.builder()
                .blog(blog)
                .writer(writer)
                .parent(parent)
                .content(content)
                .build();
        return blogCommentRepository.save(reply);
    }

    private void createAndSaveHeart(Blog blog, User user) {
        BlogHeart heart = new BlogHeart(user, blog);
        blogHeartRepository.save(heart);
        blog.plusHeart(heart);
    }

    private void createAndSaveBookmark(Blog blog, User user) {
        BlogBookmark bookmark = new BlogBookmark(user, blog);
        blogBookmarkRepository.save(bookmark);
    }
}