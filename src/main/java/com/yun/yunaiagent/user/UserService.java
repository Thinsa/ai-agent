package com.yun.yunaiagent.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final AppUserRepository repository;

    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(UserDtos.RegisterRequest request) {
        String username = required(request.username(), "username");
        String password = required(request.password(), "password");
        if (repository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        String displayName = request.displayName() == null || request.displayName().isBlank() ? username : request.displayName().trim();
        AppUser user = AppUser.create(username, passwordEncoder.encode(password), displayName, request.email());
        return repository.save(user);
    }

    @Transactional(readOnly = true)
    public AppUser authenticate(UserDtos.LoginRequest request) {
        String username = required(request.username(), "username");
        String password = required(request.password(), "password");
        AppUser user = repository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public AppUser findByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token user"));
    }

    @Transactional
    public AppUser updateProfile(String username, UserDtos.ProfileUpdateRequest request) {
        AppUser user = findByUsername(username);
        user.updateProfile(request.displayName(), request.email(), request.role(), request.bio());
        return repository.save(user);
    }

    @Transactional
    public AppUser updateAvatar(String username, String avatarUrl) {
        AppUser user = findByUsername(username);
        user.updateAvatarUrl(avatarUrl);
        return repository.save(user);
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return value.trim();
    }
}
