package com.yun.yunaiagent.user;

import java.time.LocalDateTime;

public final class UserDtos {

    private UserDtos() {
    }

    public record RegisterRequest(String username, String password, String displayName, String email) {
    }

    public record LoginRequest(String username, String password) {
    }

    public record ProfileUpdateRequest(String displayName, String email, String role, String bio) {
    }

    public record UserResponse(Long id, String username, String displayName, String email, String role, String bio,
                               LocalDateTime createdAt) {
        public static UserResponse from(AppUser user) {
            return new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getDisplayName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getBio(),
                    user.getCreatedAt()
            );
        }
    }

    public record LoginResponse(String token, UserResponse user) {
    }
}
