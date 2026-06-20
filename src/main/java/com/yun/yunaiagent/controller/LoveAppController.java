package com.yun.yunaiagent.controller;

import com.yun.yunaiagent.app.LoveApp;
import com.yun.yunaiagent.common.SecurityUtils;
import com.yun.yunaiagent.constants.Constants;
import com.yun.yunaiagent.security.JwtService;
import com.yun.yunaiagent.user.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai/love_app")
public class LoveAppController {

    private final LoveApp loveApp;
    private final UserService userService;
    private final JwtService jwtService;

    public LoveAppController(LoveApp loveApp, UserService userService, JwtService jwtService) {
        this.loveApp = loveApp;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId, Authentication authentication) {
        return loveApp.doChat(message, chatId,
                SecurityUtils.currentUser(authentication, null, jwtService, userService));
    }

    /**
     * 直接返回 Reactor Flux，适合前端按文本片段消费 SSE 数据。
     */
    @GetMapping(value = "/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(
            String message, String chatId,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) String token,
            Authentication authentication) {
        var user = SecurityUtils.currentUser(authentication, token, jwtService, userService);
        if (imageUrl != null && !imageUrl.isBlank()) {
            return loveApp.doChatByStreamWithImage(message, imageUrl, chatId, user);
        }
        return loveApp.doChatByStream(message, chatId, user);
    }

    /**
     * 将文本片段包装成标准 {@link ServerSentEvent}，便于后续增加 event、id 等元数据。
     */
    @GetMapping(value = "/chat/server_sent_event", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(
            String message, String chatId,
            @RequestParam(required = false) String token, Authentication authentication) {
        return loveApp.doChatByStream(message, chatId,
                        SecurityUtils.currentUser(authentication, token, jwtService, userService))
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * 兼容 Spring MVC 的 SSE 推送方式，用于不直接消费 Reactor 类型的调用方。
     */
    @GetMapping(value = "/chat/sse_emitter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter doChatWithLoveAppServerSseEmitter(
            String message, String chatId,
            @RequestParam(required = false) String token, Authentication authentication) {
        SseEmitter sseEmitter = new SseEmitter(Constants.SSE_EMITTER_TIMEOUT_MS);
        loveApp.doChatByStream(message, chatId,
                        SecurityUtils.currentUser(authentication, token, jwtService, userService))
                .subscribe(chunk -> sendChunk(sseEmitter, chunk),
                        sseEmitter::completeWithError, sseEmitter::complete);
        return sseEmitter;
    }

    @GetMapping("/chat/report")
    public LoveApp.LoveReport doChatWithLoveAppReport(String message, String chatId) {
        return loveApp.doChatWithReport(message, chatId);
    }

    @GetMapping("/chat/rag")
    public String doChatWithLoveAppRag(String message, String chatId,
            @RequestParam(required = false) String token, Authentication authentication) {
        return loveApp.doChatWithRag(message, chatId,
                SecurityUtils.currentUser(authentication, token, jwtService, userService));
    }

    @GetMapping("/chat/tools")
    public String doChatWithLoveAppTools(String message, String chatId) {
        return loveApp.doChatWithTools(message, chatId);
    }

    @GetMapping("/chat/mcp")
    public String doChatWithLoveAppMcp(String message, String chatId) {
        return loveApp.doChatWithMcp(message, chatId);
    }

    /**
     * SSE 发送失败时主动结束连接，避免客户端一直等待无效流。
     */
    private void sendChunk(SseEmitter sseEmitter, String chunk) {
        try {
            sseEmitter.send(chunk);
        } catch (IOException e) {
            sseEmitter.completeWithError(e);
        }
    }
}
