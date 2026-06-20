package com.yun.yunaiagent.service;

import com.yun.yunaiagent.chat.ChatHistoryService;
import com.yun.yunaiagent.common.ValidationUtils;
import com.yun.yunaiagent.user.AppUser;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.function.Supplier;

@Service
public class StreamingChatService {

    /**
     * 包装 SSE 流：执行前保存用户消息，完成后保存助手消息。
     *
     * @param module         聊天模块键
     * @param chatId         会话 ID（可空）
     * @param userMessage    用户消息（可空）
     * @param user           当前用户（可空）
     * @param historyService 聊天历史服务（可空时跳过持久化）
     * @param streamSupplier 创建内容流的工厂（调用者配置 ChatClient 提示词/工具）
     * @return 包装后的 Flux，含持久化钩子
     */
    public Flux<String> streamAndPersist(
            String module, String chatId, String userMessage,
            AppUser user, ChatHistoryService historyService,
            Supplier<Flux<String>> streamSupplier) {
        String normalizedMessage = ValidationUtils.normalize(userMessage, "空消息");
        String effectiveChatId = ValidationUtils.normalize(chatId, "default");

        if (historyService != null) {
            historyService.appendUserMessage(module, effectiveChatId, normalizedMessage, user);
        }

        StringBuilder answer = new StringBuilder();
        return streamSupplier.get()
                .doOnNext(answer::append)
                .doOnComplete(() -> {
                    if (historyService != null) {
                        historyService.appendAssistantMessage(module, effectiveChatId,
                                answer.toString(), user);
                    }
                });
    }
}
