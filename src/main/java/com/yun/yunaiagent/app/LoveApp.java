package com.yun.yunaiagent.app;

import com.yun.yunaiagent.chat.ChatHistoryService;
import com.yun.yunaiagent.rag.LoveAppRagService;
import com.yun.yunaiagent.user.AppUser;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LoveApp {

    private static final String MODULE = "love";

    private static final int MEMORY_WINDOW_SIZE = 20;

    private static final String DEFAULT_CHAT_ID = "default";

    private static final String SYSTEM_PROMPT = """
            你是“AI 恋爱大师”，请用温和、清晰、负责的中文回答用户的情感问题。
            先共情，再给具体建议，避免操控、PUA 或伤害性建议。
            """;

    private final ChatClient chatClient;

    private final LoveAppRagService ragService;

    private final ObjectProvider<ToolCallbackProvider> toolCallbackProvider;

    private final ChatHistoryService chatHistoryService;

    @Autowired
    public LoveApp(@Qualifier("openAiChatModel") ChatModel chatModel, LoveAppRagService ragService,
                   ObjectProvider<ToolCallbackProvider> toolCallbackProvider, ChatHistoryService chatHistoryService) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.ragService = ragService;
        this.toolCallbackProvider = toolCallbackProvider;
        this.chatHistoryService = chatHistoryService;
    }

    public LoveApp(ChatModel chatModel, LoveAppRagService ragService, ObjectProvider<ToolCallbackProvider> toolCallbackProvider) {
        this(chatModel, ragService, toolCallbackProvider, null);
    }

    public String doChat(String message, String chatId) {
        return doChat(message, chatId, null);
    }

    public String doChat(String message, String chatId, AppUser user) {
        String normalizedMessage = normalize(message);
        recordUserMessage(chatId, normalizedMessage, user);
        String content = basePrompt(normalizedMessage, chatId).call().content();
        recordAssistantMessage(chatId, content, user);
        return content;
    }

    public Flux<String> doChatByStream(String message, String chatId) {
        return doChatByStream(message, chatId, null);
    }

    public Flux<String> doChatByStream(String message, String chatId, AppUser user) {
        String normalizedMessage = normalize(message);
        recordUserMessage(chatId, normalizedMessage, user);
        StringBuilder answer = new StringBuilder();
        return basePrompt(normalizedMessage, chatId)
                .stream()
                .content()
                .doOnNext(answer::append)
                .doOnComplete(() -> recordAssistantMessage(chatId, answer.toString(), user));
    }

    public LoveReport doChatWithReport(String message, String chatId) {
        String content = basePrompt("""
                请基于用户问题生成一份结构化恋爱建议报告，包含标题、摘要和三条建议。
                用户问题：%s
                """.formatted(normalize(message)), chatId).call().content();
        return new LoveReport("恋爱建议报告", content, List.of("保持真诚沟通", "尊重对方边界", "必要时寻求专业帮助"));
    }

    public String doChatWithRag(String message, String chatId) {
        return doChatWithRag(message, chatId, null);
    }

    public String doChatWithRag(String message, String chatId, AppUser user) {
        String normalizedMessage = normalize(message);
        recordUserMessage(chatId, normalizedMessage, user);
        String context = ragService.retrieveContext(normalizedMessage);
        String content = basePrompt("""
                请优先参考以下知识库内容回答。
                知识库：
                %s

                用户问题：%s
                """.formatted(context, normalizedMessage), chatId).call().content();
        recordAssistantMessage(chatId, content, user);
        return content;
    }

    public String doChatWithTools(String message, String chatId) {
        ToolCallbackProvider provider = toolCallbackProvider.getIfAvailable();
        if (provider == null) {
            return "工具调用失败：未注册工具。";
        }
        return basePrompt(normalize(message), chatId)
                .toolCallbacks(provider)
                .call()
                .content();
    }

    public String doChatWithMcp(String message, String chatId) {
        ToolCallbackProvider provider = toolCallbackProvider.getIfAvailable();
        if (provider == null) {
            return "MCP 调用失败：未配置 MCP 工具提供者。";
        }
        return basePrompt("请在需要时使用 MCP 工具回答：" + normalize(message), chatId)
                .toolCallbacks(provider)
                .call()
                .content();
    }

    private ChatClient.ChatClientRequestSpec basePrompt(String message, String chatId) {
        return chatClient.prompt()
                .system(systemPromptWithMemory(chatId))
                .user(normalize(message));
    }

    private String systemPromptWithMemory(String chatId) {
        String memory = recentMemory(chatId);
        if (memory.isBlank()) {
            return SYSTEM_PROMPT;
        }
        return SYSTEM_PROMPT + System.lineSeparator() + """

                以下是当前会话最近的历史消息，请用于保持上下文连续：
                %s
                """.formatted(memory);
    }

    private String recentMemory(String chatId) {
        if (chatHistoryService == null) {
            return "";
        }
        return chatHistoryService.recentMessages(MODULE, normalizeChatId(chatId), MEMORY_WINDOW_SIZE)
                .stream()
                .map(message -> message.getRole() + ": " + message.getContent())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private void recordUserMessage(String chatId, String content, AppUser user) {
        if (chatHistoryService != null) {
            chatHistoryService.appendUserMessage(MODULE, normalizeChatId(chatId), content, user);
        }
    }

    private void recordAssistantMessage(String chatId, String content, AppUser user) {
        if (chatHistoryService != null && content != null && !content.isBlank()) {
            chatHistoryService.appendAssistantMessage(MODULE, normalizeChatId(chatId), content, user);
        }
    }

    private String normalize(String message) {
        if (message == null || message.isBlank()) {
            return "空消息";
        }
        return message.trim();
    }

    private String normalizeChatId(String chatId) {
        if (chatId == null || chatId.isBlank()) {
            return DEFAULT_CHAT_ID;
        }
        return chatId.trim();
    }

    public record LoveReport(String title, String summary, List<String> suggestions) {
    }
}
