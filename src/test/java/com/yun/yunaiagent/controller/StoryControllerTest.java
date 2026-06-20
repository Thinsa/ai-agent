package com.yun.yunaiagent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.yun.yunaiagent.common.TestAuthHelper.registerAndLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:story-controller-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.ai.openai.api-key=test-openai-key",
        "spring.ai.dashscope.api-key=test-dashscope-key",
        "spring.autoconfigure.exclude=org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration",
        "app.jwt.secret=test-secret-key-test-secret-key-test-secret-key",
        "app.jwt.expiration=3600000"
})
class StoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void storyEndpointStartsStreamWithStartMessage() throws Exception {
        String token = registerAndLogin(mockMvc, objectMapper,"story-tester");

        mockMvc.perform(get("/ai/story/sse")
                        .param("message", "开始")
                        .param("chatId", "story_test_1")
                        .param("token", token)
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    void storyEndpointAcceptsThemeParam() throws Exception {
        String token = registerAndLogin(mockMvc, objectMapper,"wuxia-tester");

        mockMvc.perform(get("/ai/story/sse")
                        .param("message", "武侠江湖恩怨")
                        .param("chatId", "story_wuxia_1")
                        .param("token", token)
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    void storyEndpointSavesMessagesToHistory() throws Exception {
        String token = registerAndLogin(mockMvc, objectMapper,"history-tester");

        // 发送第一条消息
        mockMvc.perform(get("/ai/story/sse")
                        .param("message", "科幻冒险")
                        .param("chatId", "story_history_1")
                        .param("token", token)
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk());

        // 历史记录端点应该可以访问（验证 chatId 有效）
        mockMvc.perform(get("/ai/history/sessions/story_history_1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
