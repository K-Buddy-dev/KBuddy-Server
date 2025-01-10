package com.example.kbuddy_backend.common.config;

import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.exception.UserNotFoundException;
import com.example.kbuddy_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {

        boolean isCurrentUserAnnotation = parameter.getParameterAnnotation(CurrentUser.class) != null;
        boolean isUserClass = User.class.equals(parameter.getParameterType());

        return isCurrentUserAnnotation && isUserClass;
    }

    @Nullable
    @Override
    public User resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory)
            throws Exception {
        String email = SecurityContextHolder.getContextHolderStrategy().getContext()
                .getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }
}
