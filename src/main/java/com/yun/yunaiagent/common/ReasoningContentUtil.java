package com.yun.yunaiagent.common;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * 工具类：从 ChatResponse 中提取 reasoning_content 和回答文本。
 *
 * <p>Spring AI 1.0.0 的 AssistantMessage 未直接暴露 reasoning_content，
 * 推理内容可能存放在 ChatGenerationMetadata 中，使用 "reasoningContent" 键访问。</p>
 */
public final class ReasoningContentUtil {

    /** 推理内容在 ChatGenerationMetadata 中可能的 key */
    private static final String REASONING_KEY = "reasoningContent";

    private ReasoningContentUtil() {
    }

    /**
     * 从 Generation 中提取推理内容（尝试 metadata，回退常规 text）。
     */
    private static String extractText(Generation generation) {
        AssistantMessage message = generation.getOutput();
        String text = message.getText();
        // Spring AI 1.0.0: reasoning 可能在 ChatGenerationMetadata 中
        ChatGenerationMetadata metadata = generation.getMetadata();
        String reasoning = null;
        if (metadata != null && metadata.containsKey(REASONING_KEY)) {
            Object value = metadata.get(REASONING_KEY);
            if (value instanceof String s && !s.isBlank()) {
                reasoning = s;
            }
        }
        if (reasoning != null) {
            return "【思考过程】\n" + reasoning + "\n\n【回答】\n" + (text != null ? text : "");
        }
        return text != null ? text : "";
    }

    /**
     * 同步调用：合并思考过程和回答。
     */
    public static String withReasoning(ChatResponse response) {
        return extractText(response.getResult());
    }

    /**
     * 流式调用：将 {@code Flux<ChatResponse>} 转换为 {@code Flux<String>}，
     * 逐 chunk 交错输出思考内容和回答内容。
     */
    public static Flux<String> streamWithReasoning(Flux<ChatResponse> stream) {
        return stream.flatMap(response -> {
            String text = extractText(response.getResult());
            return text.isBlank() ? Flux.empty() : Flux.just(text);
        });
    }
}
