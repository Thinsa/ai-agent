package com.yun.yunaiagent.controller;

import com.yun.yunaiagent.agent.YuManus;
import com.yun.yunaiagent.app.LoveApp;
import com.yun.yunaiagent.chat.ChatHistoryService;
import com.yun.yunaiagent.security.JwtService;
import com.yun.yunaiagent.tools.AgentTool;
import com.yun.yunaiagent.user.AppUser;
import com.yun.yunaiagent.user.UserService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.List;

@RestController
@RequestMapping("/ai")
public class AiController {

    /**
     * 恋爱大师应用入口，负责承接普通聊天、流式聊天、RAG、工具调用等能力。
     */
    private final LoveApp loveApp;

    /**
     * Spring 自动注入所有 {@link AgentTool} 实现，供智能体按需调用。
     */
    private final List<AgentTool> allTools;

    private final ChatModel chatModel;

    private final ObjectProvider<ToolCallbackProvider> toolCallbackProvider;

    private final UserService userService;

    private final JwtService jwtService;

    private final ChatHistoryService chatHistoryService;

    public AiController(LoveApp loveApp, List<AgentTool> allTools, @Qualifier("openAiChatModel") ChatModel chatModel, ObjectProvider<ToolCallbackProvider> toolCallbackProvider, UserService userService, JwtService jwtService, ChatHistoryService chatHistoryService) {
        this.loveApp = loveApp;
        this.allTools = allTools;
        this.chatModel = chatModel;
        this.toolCallbackProvider = toolCallbackProvider;
        this.userService = userService;
        this.jwtService = jwtService;
        this.chatHistoryService = chatHistoryService;
    }

    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId, Authentication authentication) {
        return loveApp.doChat(message, chatId, currentUser(authentication, null));
    }

    /**
     * 直接返回 Reactor Flux，适合前端按文本片段消费 SSE 数据。
     */
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId, @RequestParam(required = false) String token, Authentication authentication) {
        return loveApp.doChatByStream(message, chatId, currentUser(authentication, token));
    }

    /**
     * 将文本片段包装成标准 {@link ServerSentEvent}，便于后续增加 event、id 等元数据。
     */
    @GetMapping(value = "/love_app/chat/server_sent_event", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String chatId, @RequestParam(required = false) String token, Authentication authentication) {
        return loveApp.doChatByStream(message, chatId, currentUser(authentication, token))
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * 兼容 Spring MVC 的 SSE 推送方式，用于不直接消费 Reactor 类型的调用方。
     */
    @GetMapping(value = "/love_app/chat/sse_emitter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter doChatWithLoveAppServerSseEmitter(String message, String chatId, @RequestParam(required = false) String token, Authentication authentication) {
        SseEmitter sseEmitter = new SseEmitter(180000L);
        loveApp.doChatByStream(message, chatId, currentUser(authentication, token))
                .subscribe(chunk -> sendChunk(sseEmitter, chunk), sseEmitter::completeWithError, sseEmitter::complete);
        return sseEmitter;
    }

    @GetMapping("/love_app/chat/report")
    public LoveApp.LoveReport doChatWithLoveAppReport(String message, String chatId) {
        return loveApp.doChatWithReport(message, chatId);
    }

    @GetMapping("/love_app/chat/rag")
    public String doChatWithLoveAppRag(String message, String chatId, @RequestParam(required = false) String token, Authentication authentication) {
        return loveApp.doChatWithRag(message, chatId, currentUser(authentication, token));
    }

    @GetMapping("/love_app/chat/tools")
    public String doChatWithLoveAppTools(String message, String chatId) {
        return loveApp.doChatWithTools(message, chatId);
    }

    @GetMapping("/love_app/chat/mcp")
    public String doChatWithLoveAppMcp(String message, String chatId) {
        return loveApp.doChatWithMcp(message, chatId);
    }

    /**
     * 创建一次性的 Manus 智能体实例，并通过 SSE 持续返回推理/行动步骤。
     */
    @GetMapping(value = "/manus/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter doChatWithManus(String message, String chatId, @RequestParam(required = false) String token, Authentication authentication) {
        YuManus yuManus = new YuManus(allTools, chatModel, toolCallbackProvider.getIfAvailable(), chatHistoryService, chatId, currentUser(authentication, token));
        return yuManus.runStream(message);
    }

    @GetMapping(value = "/manus/chat/mcp", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter doChatWithManusMcp(String message, String chatId, @RequestParam(required = false) String token, Authentication authentication) {
        ToolCallbackProvider provider = toolCallbackProvider.getIfAvailable();
        if (provider == null) {
            SseEmitter emitter = new SseEmitter(30000L);
            sendChunk(emitter, "MCP 调用失败：未配置或未启动 MCP Server。请先启动 yu-image-search-mcp-server。");
            emitter.complete();
            return emitter;
        }
        YuManus yuManus = new YuManus(allTools, chatModel, provider, chatHistoryService, chatId, currentUser(authentication, token));
        return yuManus.runStream("请优先使用 MCP 工具完成任务：" + message);
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

    private AppUser currentUser(Authentication authentication, String token) {
        if (authentication != null) {
            return userService.findByUsername(authentication.getName());
        }
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            return userService.findByUsername(jwtService.parseUsername(token));
        } catch (Exception ignored) {
            return null;
        }
    }
}
