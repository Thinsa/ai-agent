package com.yun.yunaiagent.controller;

import com.yun.yunaiagent.security.JwtService;
import com.yun.yunaiagent.user.AppUser;
import com.yun.yunaiagent.user.UserDtos;
import com.yun.yunaiagent.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public UserDtos.UserResponse register(@RequestBody UserDtos.RegisterRequest request) {
        return UserDtos.UserResponse.from(userService.register(request));
    }

    @PostMapping("/login")
    public UserDtos.LoginResponse login(@RequestBody UserDtos.LoginRequest request) {
        AppUser user = userService.authenticate(request);
        return new UserDtos.LoginResponse(jwtService.generateToken(user.getUsername()), UserDtos.UserResponse.from(user));
    }

    @GetMapping("/current")
    public UserDtos.UserResponse current(Authentication authentication) {
        return UserDtos.UserResponse.from(userService.findByUsername(authentication.getName()));
    }

    @PutMapping("/profile")
    public UserDtos.UserResponse updateProfile(Authentication authentication, @RequestBody UserDtos.ProfileUpdateRequest request) {
        return UserDtos.UserResponse.from(userService.updateProfile(authentication.getName(), request));
    }

    @PostMapping("/logout")
    public String logout() {
        return "ok";
    }
}
