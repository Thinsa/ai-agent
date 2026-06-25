package com.yun.yunaiagent.controller;

import com.yun.yunaiagent.chat.ChatConversation;
import com.yun.yunaiagent.chat.ChatDtos;
import com.yun.yunaiagent.chat.ChatHistoryService;
import com.yun.yunaiagent.common.SecurityUtils;
import com.yun.yunaiagent.user.AppUser;
import com.yun.yunaiagent.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/history")
/**
 * 聊天历史查询接口。
 *
 * <p>按模块和会话维度返回历史对话，供侧边栏和会话恢复功能使用。</p>
 */
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;

    private final UserService userService;

    public ChatHistoryController(ChatHistoryService chatHistoryService, UserService userService) {
        this.chatHistoryService = chatHistoryService;
        this.userService = userService;
    }

    @GetMapping("/sessions")
    public List<ChatDtos.SessionSummary> listSessions(@RequestParam String module, Authentication authentication) {
        return chatHistoryService.listSessions(module, SecurityUtils.currentUser(authentication, userService));
    }

    @PostMapping("/sessions")
    public ChatDtos.SessionSummary createSession(@RequestBody Map<String, String> body, Authentication authentication) {
        AppUser user = SecurityUtils.currentUser(authentication, userService);
        ChatConversation conversation = chatHistoryService.createSession(
                body.get("module"), body.get("chatId"), user);
        return ChatDtos.SessionSummary.from(conversation);
    }

    @GetMapping("/sessions/{chatId}")
    public ChatDtos.ConversationDetail getConversation(
            @PathVariable String chatId,
            @RequestParam(required = false) String module,
            Authentication authentication
    ) {
        AppUser user = SecurityUtils.currentUser(authentication, userService);
        if (module == null || module.isBlank()) {
            return chatHistoryService.getConversation(chatId, user);
        }
        return chatHistoryService.getConversation(module, chatId, user);
    }

    @DeleteMapping("/sessions/{chatId}")
    public void deleteSession(@PathVariable String chatId, @RequestParam String module,
            Authentication authentication) {
        chatHistoryService.deleteConversation(module, chatId,
                SecurityUtils.currentUser(authentication, userService));
    }

}
