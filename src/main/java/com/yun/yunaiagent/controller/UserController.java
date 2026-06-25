package com.yun.yunaiagent.controller;

import com.yun.yunaiagent.security.JwtService;
import com.yun.yunaiagent.user.AppUser;
import com.yun.yunaiagent.user.AvatarStorageService;
import com.yun.yunaiagent.user.UserDtos;
import com.yun.yunaiagent.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
/**
 * 用户账号接口。
 *
 * <p>提供注册、登录、当前用户、token 校验、资料更新和头像上传能力。</p>
 */
public class UserController {

    private final UserService userService;

    private final JwtService jwtService;

    private final AvatarStorageService avatarStorageService;

    public UserController(UserService userService, JwtService jwtService, AvatarStorageService avatarStorageService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.avatarStorageService = avatarStorageService;
    }

    @PostMapping("/register")
    public UserDtos.UserResponse register(@RequestBody UserDtos.RegisterRequest request) {
        return UserDtos.UserResponse.from(userService.register(request));
    }

    @PostMapping("/login")
    public UserDtos.LoginResponse login(@RequestBody UserDtos.LoginRequest request) {
        AppUser user = userService.authenticate(request);
        JwtService.TokenInfo tokenInfo = jwtService.createToken(user.getUsername());
        return new UserDtos.LoginResponse(
                tokenInfo.token(),
                UserDtos.UserResponse.from(user),
                tokenInfo.expiresAt(),
                tokenInfo.expiresIn()
        );
    }

    @GetMapping("/current")
    public UserDtos.UserResponse current(Authentication authentication) {
        return UserDtos.UserResponse.from(userService.findByUsername(authentication.getName()));
    }

    @GetMapping("/token/validate")
    public UserDtos.TokenValidationResponse validateToken(@RequestHeader(value = "Authorization", required = false) String authorization) {
        // token 校验接口只解析 Authorization 头，不依赖当前 SecurityContext，便于前端启动时主动探活。
        String token = resolveBearerToken(authorization);
        return jwtService.validateToken(token)
                .map(validation -> UserDtos.TokenValidationResponse.valid(
                        validation.username(),
                        validation.expiresAt(),
                        userService.findByUsername(validation.username())
                ))
                .orElseGet(UserDtos.TokenValidationResponse::invalid);
    }

    @PutMapping("/profile")
    public UserDtos.UserResponse updateProfile(Authentication authentication, @RequestBody UserDtos.ProfileUpdateRequest request) {
        return UserDtos.UserResponse.from(userService.updateProfile(authentication.getName(), request));
    }

    @PostMapping("/avatar")
    public UserDtos.UserResponse uploadAvatar(Authentication authentication, @RequestParam("file") MultipartFile file) {
        String avatarUrl = avatarStorageService.uploadAvatar(authentication.getName(), file);
        return UserDtos.UserResponse.from(userService.updateAvatar(authentication.getName(), avatarUrl));
    }

    @PostMapping("/logout")
    public String logout() {
        return "ok";
    }

    private String resolveBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7);
    }
}
