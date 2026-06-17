package com.yun.yunaiagent.controller;

import com.yun.yunaiagent.chat.ChatDtos;
import com.yun.yunaiagent.chat.ChatHistoryService;
import com.yun.yunaiagent.user.AppUser;
import com.yun.yunaiagent.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ai/history")
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;

    private final UserService userService;

    public ChatHistoryController(ChatHistoryService chatHistoryService, UserService userService) {
        this.chatHistoryService = chatHistoryService;
        this.userService = userService;
    }

    @GetMapping("/sessions")
    public List<ChatDtos.SessionSummary> listSessions(@RequestParam String module, Authentication authentication) {
        return chatHistoryService.listSessions(module, currentUser(authentication));
    }

    @GetMapping("/sessions/{chatId}")
    public ChatDtos.ConversationDetail getConversation(@PathVariable String chatId, Authentication authentication) {
        return chatHistoryService.getConversation(chatId, currentUser(authentication));
    }

    private AppUser currentUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        return userService.findByUsername(authentication.getName());
    }
}
