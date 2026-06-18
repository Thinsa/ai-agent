package com.yun.yunaiagent.storysave;

import com.yun.yunaiagent.user.AppUser;
import com.yun.yunaiagent.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/story-saves")
public class StorySaveController {

    private final StorySaveService saveService;
    private final UserService userService;

    public StorySaveController(StorySaveService saveService, UserService userService) {
        this.saveService = saveService;
        this.userService = userService;
    }

    @PostMapping
    public StorySave createSave(@RequestBody Map<String, String> body, Authentication authentication) {
        String storyId = body.get("storyId");
        String sceneId = body.get("sceneId");
        String choiceHistory = body.getOrDefault("choiceHistory", "");
        if (storyId == null || storyId.isBlank() || sceneId == null || sceneId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "storyId and sceneId are required");
        }
        Long userId = currentUserId(authentication);
        return saveService.createSave(userId, storyId, sceneId, choiceHistory);
    }

    @GetMapping
    public List<StorySave> listSaves(Authentication authentication) {
        Long userId = currentUserId(authentication);
        return saveService.listSaves(userId, "lingqiao");
    }

    @DeleteMapping("/{saveId}")
    public void deleteSave(@PathVariable Long saveId, Authentication authentication) {
        Long userId = currentUserId(authentication);
        saveService.deleteSave(userId, saveId);
    }

    private Long currentUserId(Authentication authentication) {
        AppUser user = userService.findByUsername(authentication.getName());
        return user.getId();
    }
}
