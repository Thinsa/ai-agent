package com.yun.yunaiagent.controller;

import com.yun.yunaiagent.agent.YuManus;
import com.yun.yunaiagent.app.LoveApp;
import com.yun.yunaiagent.constants.Constants;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import com.yun.yunaiagent.chat.ChatHistoryService;
import com.yun.yunaiagent.chat.ChatMessage;
import com.yun.yunaiagent.security.JwtService;
import com.yun.yunaiagent.tools.AgentTool;
import com.yun.yunaiagent.tools.OssObjectStorageService;
import com.yun.yunaiagent.user.AppUser;
import com.yun.yunaiagent.user.UserService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private final OssObjectStorageService ossService;

    public AiController(LoveApp loveApp, List<AgentTool> allTools, @Qualifier("openAiChatModel") ChatModel chatModel, ObjectProvider<ToolCallbackProvider> toolCallbackProvider, UserService userService, JwtService jwtService, ChatHistoryService chatHistoryService, OssObjectStorageService ossService) {
        this.loveApp = loveApp;
        this.allTools = allTools;
        this.chatModel = chatModel;
        this.toolCallbackProvider = toolCallbackProvider;
        this.userService = userService;
        this.jwtService = jwtService;
        this.chatHistoryService = chatHistoryService;
        this.ossService = ossService;
    }

    @GetMapping(value = "/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatBasic(String message, String chatId, @RequestParam(required = false) String token, Authentication authentication) {
        AppUser user = currentUser(authentication, token);
        String normalizedMessage = message == null || message.isBlank() ? "空消息" : message.trim();
        if (chatHistoryService != null) {
            chatHistoryService.appendUserMessage("chat", chatId == null || chatId.isBlank() ? "default" : chatId.trim(), normalizedMessage, user);
        }
        StringBuilder answer = new StringBuilder();
        return ChatClient.builder(chatModel).build().prompt()
                .system("你是灵语 Spark，LinkMind 灵桥的日常对话伙伴。请用友好、自然的中文回答用户的问题，保持回答简洁有温度。")
                .user(normalizedMessage)
                .stream()
                .content()
                .doOnNext(answer::append)
                .doOnComplete(() -> {
                    if (chatHistoryService != null) {
                        chatHistoryService.appendAssistantMessage("chat",
                                chatId == null || chatId.isBlank() ? "default" : chatId.trim(),
                                answer.toString(), user);
                    }
                });
    }

    @PostMapping("/upload")
    public Map<String, String> uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String token, Authentication authentication) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        try {
            currentUser(authentication, token);

            String originalName = file.getOriginalFilename();
            String safeName = (originalName != null && !originalName.isBlank())
                    ? originalName : "upload_" + System.currentTimeMillis();
            String contentType = file.getContentType();
            boolean isImage = contentType != null && contentType.startsWith("image/");

            if (isImage) {
                // Image upload to OSS for public URL
                String objectKey = "uploads/images/" + System.currentTimeMillis() + "_" + safeName;
                String imageUrl = ossService.upload(objectKey, file.getBytes(), contentType);
                return Map.of(
                        "fileName", safeName,
                        "filePath", objectKey,
                        "contentType", contentType,
                        "imageUrl", imageUrl,
                        "preview", "",
                        "isImage", "true"
                );
            }

            // Non-image files saved to local workspace
            Path workspaceDir = Path.of(System.getProperty("user.dir"), "workspace", "uploads");
            Files.createDirectories(workspaceDir);
            Path target = workspaceDir.resolve(safeName);
            file.transferTo(target);

            String preview = "";
            if (contentType != null && (contentType.startsWith("text/") ||
                    safeName.matches(".*\\.(txt|md|json|xml|csv|log|java|py|js|html|css|yml|yaml|properties)$"))) {
                try {
                    String content = Files.readString(target);
                    preview = content.length() > Constants.FILE_PREVIEW_MAX_CHARS ? content.substring(0, Constants.FILE_PREVIEW_MAX_CHARS) + "\n...(truncated)" : content;
                } catch (IOException ignored) {
                }
            }

            return Map.of(
                    "fileName", safeName,
                    "filePath", target.toString(),
                    "contentType", contentType != null ? contentType : "application/octet-stream",
                    "preview", preview,
                    "isImage", "false"
            );
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed: " + e.getMessage(), e);
        }
    }

    private static final String STORY_SYSTEM_PROMPT = """
            你是一个互动故事剧本的主持人。根据用户的主题创作分支式互动故事。

            规则：
            1. 用户说"开始"或给出主题（如"武侠""科幻""悬疑"），你立刻开始讲述故事背景和开场
            2. 每次输出：先写 2-4 段剧情，然后给出选项让用户选择下一步
            3. 选项必须严格使用以下格式（放在消息末尾）：
            【选项】
            1. 第一个选项描述
            2. 第二个选项描述
            3. 第三个选项描述
            （可选第 4 个选项）
            5. 我自己决定（用户自行输入行动）

            4. 选项数量：2-4 个具体选项 + 第 5 个固定为"我自己决定"
            5. 用户回复数字（1-5）或自定义行动，你据此继续剧情
            6. 当故事到达结局时，输出【结局】标记，然后写一段结局总结
            7. 结局后提示用户："输入「开始」或一个新主题来开启新故事"

            创作要求：
            - 每个故事至少有 3 种不同结局（好结局、坏结局、隐藏结局）
            - 每个分支约 3-5 轮选择到达结局
            - 选项要有张力和意义，不同选择导向明显不同的剧情走向
            - 语言生动，有画面感，让用户沉浸在故事中
            - 如果用户中途想换故事，直接响应"开始"即可
            """;

    @GetMapping(value = "/story/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doStoryChat(String message, String chatId,
            @RequestParam(required = false) String token, Authentication authentication) {
        AppUser user = currentUser(authentication, token);
        String normalizedMessage = message == null || message.isBlank() ? "空消息" : message.trim();
        String effectiveChatId = chatId == null || chatId.isBlank() ? "story_default" : chatId.trim();

        // 保存用户消息
        if (chatHistoryService != null) {
            chatHistoryService.appendUserMessage("chat", effectiveChatId, normalizedMessage, user);
        }

        // 加载最近对话历史（最多 30 条），构建 Prompt
        List<Message> history = List.of();
        if (chatHistoryService != null) {
            List<ChatMessage> recent = chatHistoryService.recentMessages("chat", effectiveChatId, 30, user);
            history = recent.stream()
                    .map(m -> m.getRole().equals("user")
                            ? (Message) new UserMessage(m.getContent())
                            : (Message) new AssistantMessage(m.getContent()))
                    .collect(Collectors.toList());
        }

        List<Message> allMessages = new ArrayList<>();
        allMessages.add(new SystemMessage(STORY_SYSTEM_PROMPT));
        allMessages.addAll(history);
        allMessages.add(new UserMessage(normalizedMessage));

        StringBuilder answer = new StringBuilder();
        return ChatClient.builder(chatModel).build()
                .prompt(new Prompt(allMessages))
                .stream()
                .content()
                .doOnNext(answer::append)
                .doOnComplete(() -> {
                    if (chatHistoryService != null) {
                        chatHistoryService.appendAssistantMessage("chat",
                                effectiveChatId, answer.toString(), user);
                    }
                });
    }

    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId, Authentication authentication) {
        return loveApp.doChat(message, chatId, currentUser(authentication, null));
    }

    /**
     * 直接返回 Reactor Flux，适合前端按文本片段消费 SSE 数据。
     */
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(
            String message, String chatId,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) String token,
            Authentication authentication) {
        AppUser user = currentUser(authentication, token);
        if (imageUrl != null && !imageUrl.isBlank()) {
            return loveApp.doChatByStreamWithImage(message, imageUrl, chatId, user);
        }
        return loveApp.doChatByStream(message, chatId, user);
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
        SseEmitter sseEmitter = new SseEmitter(Constants.SSE_EMITTER_TIMEOUT_MS);
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
            SseEmitter emitter = new SseEmitter(Constants.MCP_ERROR_TIMEOUT_MS);
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
