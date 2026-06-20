package com.yun.yunaiagent.controller;

import com.yun.yunaiagent.agent.YuManus;
import com.yun.yunaiagent.chat.ChatHistoryService;
import com.yun.yunaiagent.security.JwtService;
import com.yun.yunaiagent.tools.AgentTool;
import com.yun.yunaiagent.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ManusControllerTest {

    @Test
    void manusMcpReturnsClearErrorWhenToolProviderIsMissing() {
        ManusController controller = new ManusController(
                List.of(),
                mock(ChatModel.class),
                emptyProvider(),
                mock(ChatHistoryService.class),
                mock(UserService.class),
                mock(JwtService.class)
        );

        SseEmitter emitter = controller.doChatWithManusMcp("search cat image", "mcp-test", null, null);

        assertThat(emitter).isNotNull();
    }

    @Test
    void manusChatUsesAvailableToolProviderForLocalTools() {
        ToolCallbackProvider provider = mock(ToolCallbackProvider.class);
        TrackingProvider trackingProvider = new TrackingProvider(provider);
        ManusController controller = new ManusController(
                List.of(),
                mock(ChatModel.class),
                trackingProvider,
                mock(ChatHistoryService.class),
                mock(UserService.class),
                mock(JwtService.class)
        );

        SseEmitter emitter = controller.doChatWithManus("hello", "chat-test", null, null, null, null);

        assertThat(emitter).isNotNull();
        assertThat(trackingProvider.getIfAvailableCalled).isTrue();
        assertThat(trackingProvider.getIfUniqueCalled).isFalse();
    }

    private static ObjectProvider<ToolCallbackProvider> emptyProvider() {
        return new ObjectProvider<>() {
            @Override
            public ToolCallbackProvider getObject(Object... args) {
                return null;
            }

            @Override
            public ToolCallbackProvider getIfAvailable() {
                return null;
            }

            @Override
            public ToolCallbackProvider getIfUnique() {
                return null;
            }

            @Override
            public ToolCallbackProvider getObject() {
                return null;
            }
        };
    }

    private static class TrackingProvider implements ObjectProvider<ToolCallbackProvider> {

        private final ToolCallbackProvider provider;

        private boolean getIfAvailableCalled;

        private boolean getIfUniqueCalled;

        private TrackingProvider(ToolCallbackProvider provider) {
            this.provider = provider;
        }

        @Override
        public ToolCallbackProvider getObject(Object... args) {
            return provider;
        }

        @Override
        public ToolCallbackProvider getIfAvailable() {
            getIfAvailableCalled = true;
            return provider;
        }

        @Override
        public ToolCallbackProvider getIfUnique() {
            getIfUniqueCalled = true;
            return provider;
        }

        @Override
        public ToolCallbackProvider getObject() {
            return provider;
        }
    }
}
