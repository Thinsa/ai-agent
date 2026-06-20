package com.yun.yunaiagent.app;

import com.yun.yunaiagent.chat.ChatHistoryService;
import com.yun.yunaiagent.rag.EmptyKnowledgeDocumentRepository;
import com.yun.yunaiagent.rag.LoveAppDocumentLoader;
import com.yun.yunaiagent.rag.LoveAppRagService;
import com.yun.yunaiagent.rag.QueryRewriter;
import com.yun.yunaiagent.service.StreamingChatService;
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
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoveAppTest {

    @Test
    void doChatUsesChatModelContent() {
        LoveApp loveApp = new LoveApp(new FakeChatModel(), fakeRagService(), emptyProvider());

        String response = loveApp.doChat("hello", "chat-1");

        assertThat(response).contains("model response");
    }

    @Test
    void doChatByStreamReturnsModelChunks() {
        StreamingChatService streamingChatService = mock(StreamingChatService.class);
        when(streamingChatService.streamAndPersist(any(), any(), any(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    Supplier<Flux<String>> supplier = invocation.getArgument(5);
                    return supplier.get();
                });
        LoveApp loveApp = new LoveApp(new FakeChatModel(), fakeRagService(), emptyProvider(),
                null, streamingChatService);

        List<String> chunks = loveApp.doChatByStream("hello", "chat-1").collectList().block();

        assertThat(chunks).containsExactly("stream", "response");
    }

    @Test
    void doChatWithRagRecordsConversationHistory() {
        ChatHistoryService chatHistoryService = mock(ChatHistoryService.class);
        LoveApp loveApp = new LoveApp(new FakeChatModel(), fakeRagService(), emptyProvider(), chatHistoryService, null);

        String response = loveApp.doChatWithRag("How to date well?", "love-rag-1", null);

        assertThat(response).contains("model response");
        verify(chatHistoryService).appendUserMessage(eq("love"), eq("love-rag-1"), eq("How to date well?"), isNull());
        verify(chatHistoryService).appendAssistantMessage(eq("love"), eq("love-rag-1"), eq(response), isNull());
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
        }, new EmptyKnowledgeDocumentRepository());
    }

    private static class FakeChatModel implements ChatModel {

        @Override
        public ChatResponse call(Prompt prompt) {
            return new ChatResponse(List.of(new Generation(new AssistantMessage("model response: " + prompt.getContents()))));
        }

        @Override
        public Flux<ChatResponse> stream(Prompt prompt) {
            return Flux.just(
                    new ChatResponse(List.of(new Generation(new AssistantMessage("stream")))),
                    new ChatResponse(List.of(new Generation(new AssistantMessage("response"))))
            );
        }

        @Override
        public ChatOptions getDefaultOptions() {
            return null;
        }
    }
}
