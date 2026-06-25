package com.yun.yunaiagent.user;

import java.time.LocalDateTime;

/**
 * 用户模块请求和响应 DTO 集合。
 */
public final class UserDtos {

    private UserDtos() {
    }

    /**
     * 用户注册请求。
     */
    public record RegisterRequest(String username, String password, String displayName, String email) {
    }

    /**
     * 用户登录请求。
     */
    public record LoginRequest(String username, String password) {
    }

    /**
     * 用户资料更新请求。
     */
    public record ProfileUpdateRequest(String displayName, String email, String role, String bio) {
    }

    /**
     * 用户信息响应。
     */
    public record UserResponse(Long id, String username, String displayName, String email, String role, String bio,
                               String avatarUrl, LocalDateTime createdAt) {
        public static UserResponse from(AppUser user) {
            return new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getDisplayName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getBio(),
                    user.getAvatarUrl(),
                    user.getCreatedAt()
            );
        }
    }

    /**
     * 登录成功响应。
     */
    public record LoginResponse(String token, UserResponse user, long expiresAt, long expiresIn) {
    }

    /**
     * 令牌校验响应。
     */
    public record TokenValidationResponse(boolean valid, String username, Long expiresAt, UserResponse user) {
        public static TokenValidationResponse invalid() {
            return new TokenValidationResponse(false, null, null, null);
        }

        public static TokenValidationResponse valid(String username, long expiresAt, AppUser user) {
            return new TokenValidationResponse(true, username, expiresAt, UserResponse.from(user));
        }
    }
}
