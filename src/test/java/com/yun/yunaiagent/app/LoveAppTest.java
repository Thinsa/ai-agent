package com.yun.yunaiagent.app;

import com.yun.yunaiagent.rag.LoveAppDocumentLoader;
import com.yun.yunaiagent.rag.LoveAppRagService;
import com.yun.yunaiagent.rag.QueryRewriter;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.ObjectProvider;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoveAppTest {

    @Test
    void doChatUsesChatModelContent() {
        LoveApp loveApp = new LoveApp(new FakeChatModel(), fakeRagService(), emptyProvider());

        String response = loveApp.doChat("你好", "chat-1");

        assertThat(response).contains("模型回复");
    }

    @Test
    void doChatByStreamReturnsModelChunks() {
        LoveApp loveApp = new LoveApp(new FakeChatModel(), fakeRagService(), emptyProvider());

        List<String> chunks = loveApp.doChatByStream("你好", "chat-1").collectList().block();

        assertThat(chunks).containsExactly("流式", "回复");
    }

    private static ObjectProvider<org.springframework.ai.tool.ToolCallbackProvider> emptyProvider() {
        return new ObjectProvider<>() {
            @Override
            public org.springframework.ai.tool.ToolCallbackProvider getObject(Object... args) {
                return null;
            }

            @Override
            public org.springframework.ai.tool.ToolCallbackProvider getIfAvailable() {
                return null;
            }

            @Override
            public org.springframework.ai.tool.ToolCallbackProvider getIfUnique() {
                return null;
            }

            @Override
            public org.springframework.ai.tool.ToolCallbackProvider getObject() {
                return null;
            }
        };
    }

    private static LoveAppRagService fakeRagService() {
        return new LoveAppRagService(new LoveAppDocumentLoader(), new QueryRewriter(new FakeChatModel()), new ObjectProvider<>() {
            @Override
            public org.springframework.ai.vectorstore.VectorStore getObject(Object... args) {
                return null;
            }

            @Override
            public org.springframework.ai.vectorstore.VectorStore getIfAvailable() {
                return null;
            }

            @Override
            public org.springframework.ai.vectorstore.VectorStore getIfUnique() {
                return null;
            }

            @Override
            public org.springframework.ai.vectorstore.VectorStore getObject() {
                return null;
            }
        });
    }

    private static class FakeChatModel implements ChatModel {

        @Override
        public ChatResponse call(Prompt prompt) {
            return new ChatResponse(List.of(new Generation(new AssistantMessage("模型回复：" + prompt.getContents()))));
        }

        @Override
        public Flux<ChatResponse> stream(Prompt prompt) {
            return Flux.just(
                    new ChatResponse(List.of(new Generation(new AssistantMessage("流式")))),
                    new ChatResponse(List.of(new Generation(new AssistantMessage("回复"))))
            );
        }

        @Override
        public ChatOptions getDefaultOptions() {
            return null;
        }
    }
}
