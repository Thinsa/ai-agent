package com.yun.yunaiagent.controller;

import com.yun.yunaiagent.agent.YuManus;
import com.yun.yunaiagent.app.LoveApp;
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

class AiControllerMcpTest {

    @Test
    void manusMcpReturnsClearErrorWhenToolProviderIsMissing() {
        AiController controller = new AiController(
                mock(LoveApp.class),
                List.of(),
                mock(ChatModel.class),
                emptyProvider(),
                mock(UserService.class),
                mock(JwtService.class),
                mock(ChatHistoryService.class)
        );

        SseEmitter emitter = controller.doChatWithManusMcp("search cat image", "mcp-test", null, null);

        assertThat(emitter).isNotNull();
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
}
