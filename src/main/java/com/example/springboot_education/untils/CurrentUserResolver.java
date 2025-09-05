package com.example.springboot_education.untils;


import com.example.springboot_education.annotations.CurrentUser;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.exceptions.UnauthorizedException;
import com.example.springboot_education.repositories.UsersJpaRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class CurrentUserResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenUtil jwtTokenUtil;
    private final UsersJpaRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(Users.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new UnauthorizedException("Token không hợp lệ hoặc bị thiếu");
        }

        token = token.substring(7);
        Integer userId = jwtTokenUtil.getUserIdFromToken(token);

        return userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Người dùng không tồn tại"));
    }
}