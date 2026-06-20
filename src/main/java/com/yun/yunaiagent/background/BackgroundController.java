package com.yun.yunaiagent.background;

import com.yun.yunaiagent.common.SecurityUtils;
import com.yun.yunaiagent.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/backgrounds")
public class BackgroundController {

    private static final double MIN_OPACITY = 0.05;
    private static final double MAX_OPACITY = 0.8;
    private static final double DEFAULT_OPACITY = 0.15;

    private final BackgroundService backgroundService;
    private final UserService userService;

    public BackgroundController(BackgroundService backgroundService, UserService userService) {
        this.backgroundService = backgroundService;
        this.userService = userService;
    }

    @GetMapping
    public Map<String, BackgroundService.BackgroundDto> listBackgrounds(Authentication authentication) {
        return backgroundService.listBackgrounds(SecurityUtils.currentUserId(authentication, userService));
    }

    @PostMapping("/{agentKey}")
    public BackgroundService.BackgroundDto saveBackground(
            @PathVariable String agentKey,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "opacity", defaultValue = "0.15") double opacity,
            Authentication authentication) {
        if (agentKey == null || agentKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "agentKey is required");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is required");
        }

        double normalizedOpacity = opacity <= 0 ? DEFAULT_OPACITY : Math.clamp(opacity, MIN_OPACITY, MAX_OPACITY);
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            contentType = "image/png";
        }

        try {
            byte[] imageBytes = file.getBytes();
            return backgroundService.saveBackground(SecurityUtils.currentUserId(authentication, userService), agentKey.trim(),
                    imageBytes, contentType, normalizedOpacity);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image", e);
        }
    }

    @PostMapping("/{agentKey}/opacity")
    public BackgroundService.BackgroundDto updateOpacity(
            @PathVariable String agentKey,
            @RequestBody OpacityRequest request,
            Authentication authentication) {
        double rawOpacity = request.opacity <= 0 ? DEFAULT_OPACITY : request.opacity;
        double normalizedOpacity = Math.clamp(rawOpacity, MIN_OPACITY, MAX_OPACITY);
        return backgroundService.updateOpacity(SecurityUtils.currentUserId(authentication, userService), agentKey.trim(), normalizedOpacity);
    }

    @DeleteMapping("/{agentKey}")
    public void deleteBackground(@PathVariable String agentKey, Authentication authentication) {
        backgroundService.deleteBackground(SecurityUtils.currentUserId(authentication, userService), agentKey);
    }

    public record OpacityRequest(double opacity) {}
}
