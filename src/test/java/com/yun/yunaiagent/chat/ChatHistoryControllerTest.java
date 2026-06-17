package com.yun.yunaiagent.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yun.yunaiagent.user.AppUser;
import com.yun.yunaiagent.user.UserDtos;
import com.yun.yunaiagent.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chat-history-controller-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
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
class ChatHistoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserService userService;

    @Autowired
    ChatHistoryService chatHistoryService;

    @Test
    void listSessionsUsesCurrentJwtUserAndDetailBlocksOtherUsersConversation() throws Exception {
        String aliceToken = registerAndLogin("alice");
        String bobToken = registerAndLogin("bob");
        AppUser alice = userService.findByUsername("alice");
        AppUser bob = userService.findByUsername("bob");

        chatHistoryService.appendUserMessage("love", "love_alice", "Alice question", alice);
        chatHistoryService.appendUserMessage("love", "love_bob", "Bob question", bob);

        mockMvc.perform(get("/ai/history/sessions")
                        .param("module", "love")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].chatId").value("love_alice"))
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/ai/history/sessions/love_bob")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/ai/history/sessions/love_bob")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId").value("love_bob"))
                .andExpect(jsonPath("$.messages[0].content").value("Bob question"));
    }

    private String registerAndLogin(String username) throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", "password123",
                                "displayName", username,
                                "email", username + "@example.com"
                        ))))
                .andExpect(status().isOk());

        String response = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", username, "password", "password123"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }
}
