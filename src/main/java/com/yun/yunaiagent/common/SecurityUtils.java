package com.yun.yunaiagent.common;

import com.yun.yunaiagent.security.JwtService;
import com.yun.yunaiagent.user.AppUser;
import com.yun.yunaiagent.user.UserService;
import org.springframework.security.core.Authentication;

public final class SecurityUtils {
    private SecurityUtils() {}

    /** 从 Authentication 解析当前用户（无 token 回退）。 */
    public static AppUser currentUser(Authentication authentication, UserService userService) {
        if (authentication == null) return null;
        return userService.findByUsername(authentication.getName());
    }

    /** 从 Authentication 优先解析，回退到 token 查询参数。 */
    public static AppUser currentUser(
            Authentication authentication, String tokenParam,
            JwtService jwtService, UserService userService) {
        if (authentication != null) return userService.findByUsername(authentication.getName());
        if (tokenParam == null || tokenParam.isBlank()) return null;
        try { return userService.findByUsername(jwtService.parseUsername(tokenParam)); }
        catch (Exception ignored) { return null; }
    }

    /** 从 Authentication 解析当前用户 ID。 */
    public static Long currentUserId(Authentication authentication, UserService userService) {
        if (authentication == null) return null;
        return userService.findByUsername(authentication.getName()).getId();
    }
}
