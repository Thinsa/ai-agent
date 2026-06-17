package com.yun.yunaiagent.rag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QueryRewriterTest {

    @Test
    void rewriteUsesChatModel() {
        QueryRewriter rewriter = new QueryRewriter(new FakeChatModel());

        assertThat(rewriter.rewrite("我总是吵架怎么办")).isEqualTo("关系争吵 沟通");
    }

    private static class FakeChatModel implements ChatModel {

        @Override
        public ChatResponse call(Prompt prompt) {
            return new ChatResponse(List.of(new Generation(new AssistantMessage("关系争吵 沟通"))));
        }

        @Override
        public Flux<ChatResponse> stream(Prompt prompt) {
            return Flux.empty();
        }

        @Override
        public ChatOptions getDefaultOptions() {
            return null;
        }
    }
}
