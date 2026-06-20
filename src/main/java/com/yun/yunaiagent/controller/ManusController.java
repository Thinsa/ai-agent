package com.yun.yunaiagent.controller;

import com.yun.yunaiagent.agent.YuManus;
import com.yun.yunaiagent.chat.ChatHistoryService;
import com.yun.yunaiagent.common.SecurityUtils;
import com.yun.yunaiagent.constants.Constants;
import com.yun.yunaiagent.security.JwtService;
import com.yun.yunaiagent.tools.AgentTool;
import com.yun.yunaiagent.user.UserService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/ai/manus")
public class ManusController {

    private final List<AgentTool> allTools;
    private final ChatModel chatModel;
    private final ObjectProvider<ToolCallbackProvider> toolCallbackProvider;
    private final ChatHistoryService chatHistoryService;
    private final UserService userService;
    private final JwtService jwtService;

    public ManusController(List<AgentTool> allTools,
            @Qualifier("openAiChatModel") ChatModel chatModel,
            ObjectProvider<ToolCallbackProvider> toolCallbackProvider,
            ChatHistoryService chatHistoryService,
            UserService userService,
            JwtService jwtService) {
        this.allTools = allTools;
        this.chatModel = chatModel;
        this.toolCallbackProvider = toolCallbackProvider;
        this.chatHistoryService = chatHistoryService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * 创建一次性的 Manus 智能体实例，并通过 SSE 持续返回推理/行动步骤。
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter doChatWithManus(String message, String chatId,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) String token, Authentication authentication) {
        YuManus yuManus = new YuManus(allTools, chatModel,
                toolCallbackProvider.getIfAvailable(), chatHistoryService, chatId,
                SecurityUtils.currentUser(authentication, token, jwtService, userService));
        yuManus.setImageUrl(imageUrl);
        yuManus.setFileName(fileName);
        return yuManus.runStream(message);
    }

    @GetMapping(value = "/chat/mcp", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter doChatWithManusMcp(String message, String chatId,
            @RequestParam(required = false) String token, Authentication authentication) {
        ToolCallbackProvider provider = toolCallbackProvider.getIfAvailable();
        if (provider == null) {
            SseEmitter emitter = new SseEmitter(Constants.MCP_ERROR_TIMEOUT_MS);
            sendChunk(emitter, "MCP 调用失败：未配置或未启动 MCP Server。请先启动 yu-image-search-mcp-server。");
            emitter.complete();
            return emitter;
        }
        YuManus yuManus = new YuManus(allTools, chatModel, provider, chatHistoryService, chatId,
                SecurityUtils.currentUser(authentication, token, jwtService, userService));
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
}
