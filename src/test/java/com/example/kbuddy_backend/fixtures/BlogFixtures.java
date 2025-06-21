package com.example.kbuddy_backend.fixtures;

import com.example.kbuddy_backend.blog.dto.request.BlogSaveRequest;
import com.example.kbuddy_backend.blog.dto.response.BlogResponse;
import com.example.kbuddy_backend.blog.dto.response.BlogPaginationResponse;
import com.example.kbuddy_backend.common.constant.ImageFileType;
import com.example.kbuddy_backend.common.dto.ImageFileDto;
import com.example.kbuddy_backend.blog.constant.BlogStatus;
import java.time.LocalDateTime;
import java.util.List;



// public class BlogFixtures {

//     public static BlogSaveRequest createBlogSaveRequest() {
//         return BlogSaveRequest.of("title", "description", List.of("cafe","theater"), List.of(1,2), BlogStatus.PUBLISHED);
//     }

//     public static BlogResponse createBlogResponse() {
//         return BlogResponse.of(
//                 1L,
//                 "test-uuid",
//                 "test-writer",
//                 "test-profile-url",
//                 List.of(1, 2),
//                 "title",
//                 "description",
//                 0,
//                 LocalDateTime.now(),
//                 LocalDateTime.now(),
//                 List.of(ImageFileDto.of(1L, ImageFileType.PNG, "test_pic", "test_url")),
//                 null,
//                 0,
//                 0,
//                 true,
//                 true,
//                 BlogStatus.PUBLISHED
//         );
//     }
// }