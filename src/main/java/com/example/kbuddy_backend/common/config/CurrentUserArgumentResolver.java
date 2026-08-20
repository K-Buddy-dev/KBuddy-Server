package com.example.kbuddy_backend.common.config;

import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.exception.UserNotFoundException;
import com.example.kbuddy_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
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
        CurrentUser annotation = parameter.getParameterAnnotation(CurrentUser.class);
        boolean required = annotation == null || annotation.required();

        Authentication authentication = SecurityContextHolder.getContextHolderStrategy().getContext()
                .getAuthentication();

        //비로그인(익명) 요청: required=false인 경우에만 null을 주입한다.
        if (!isAuthenticated(authentication)) {
            if (required) {
                throw new AccessDeniedException("유효하지 않은 인증 정보입니다.");
            }
            return null;
        }

        long id;
        try {
            id = Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            if (required) {
                throw new AccessDeniedException("유효하지 않은 인증 정보입니다.");
            }
            return null;
        }

        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    private boolean isAuthenticated(@Nullable Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
