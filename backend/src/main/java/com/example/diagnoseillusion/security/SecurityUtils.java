package com.example.diagnoseillusion.security;

import com.example.diagnoseillusion.common.CustomException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long getCurrentUserId() {
        UserPrincipal principal = getCurrentPrincipal();
        return principal.getId();
    }

    public static UserPrincipal getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new CustomException(401, "未认证，请先登录");
        }
        return principal;
    }
}
