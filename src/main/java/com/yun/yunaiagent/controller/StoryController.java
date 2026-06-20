package com.yun.yunaiagent.controller;

import com.yun.yunaiagent.chat.ChatHistoryService;
import com.yun.yunaiagent.chat.ChatMessage;
import com.yun.yunaiagent.common.SecurityUtils;
import com.yun.yunaiagent.common.ValidationUtils;
import com.yun.yunaiagent.security.JwtService;
import com.yun.yunaiagent.service.StreamingChatService;
import com.yun.yunaiagent.user.UserService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai")
public class StoryController {

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

    private static final int STORY_HISTORY_WINDOW = 30;

    private final ChatModel chatModel;
    private final ChatHistoryService chatHistoryService;
    private final StreamingChatService streamingChatService;
    private final UserService userService;
    private final JwtService jwtService;

    public StoryController(
            @Qualifier("openAiChatModel") ChatModel chatModel,
            ChatHistoryService chatHistoryService,
            StreamingChatService streamingChatService,
            UserService userService,
            JwtService jwtService) {
        this.chatModel = chatModel;
        this.chatHistoryService = chatHistoryService;
        this.streamingChatService = streamingChatService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping(value = "/story/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doStoryChat(String message, String chatId,
            @RequestParam(required = false) String token, Authentication authentication) {
        var user = SecurityUtils.currentUser(authentication, token, jwtService, userService);
        return streamingChatService.streamAndPersist("chat", chatId, message, user, chatHistoryService, () -> {
            String effectiveChatId = ValidationUtils.normalize(chatId, "story_default");
            String normalizedMessage = ValidationUtils.normalize(message, "空消息");

            // 加载最近对话历史，构建 Prompt
            List<Message> history = List.of();
            if (chatHistoryService != null) {
                List<ChatMessage> recent = chatHistoryService.recentMessages("chat", effectiveChatId, STORY_HISTORY_WINDOW, user);
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

            return ChatClient.builder(chatModel).build()
                    .prompt(new Prompt(allMessages))
                    .stream()
                    .content();
        });
    }
}
