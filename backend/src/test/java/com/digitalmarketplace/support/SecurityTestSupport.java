package com.digitalmarketplace.support;

import com.digitalmarketplace.entity.UserRole;
import com.digitalmarketplace.security.UserPrincipal;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@TestConfiguration
public class SecurityTestSupport implements WebMvcConfigurer {

    public static UserPrincipal principal(long id, String email, UserRole role) {
        return new UserPrincipal(id, email, "dummy", role.name());
    }

    public static void authenticate(long id, String email, UserRole role) {
        UserPrincipal principal = principal(id, email, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthenticationPrincipalArgumentResolver());
    }
}
