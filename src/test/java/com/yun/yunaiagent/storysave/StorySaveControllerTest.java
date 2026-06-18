package com.yun.yunaiagent.storysave;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:story-save-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
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
class StorySaveControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createsAndListsSaves() throws Exception {
        String token = registerAndLogin("save-tester");

        // 创建存档
        mockMvc.perform(post("/story-saves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "storyId", "lingqiao",
                                "sceneId", "bamboo_forest",
                                "choiceHistory", "[{\"from\":\"start\",\"choice\":\"走向竹林深处\",\"index\":0}]"
                        )))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sceneId").value("bamboo_forest"));

        // 列表
        mockMvc.perform(get("/story-saves")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deletesSave() throws Exception {
        String token = registerAndLogin("delete-save-tester");

        String response = mockMvc.perform(post("/story-saves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "storyId", "lingqiao",
                                "sceneId", "village",
                                "choiceHistory", "[]"
                        )))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int saveId = objectMapper.readTree(response).get("id").asInt();

        mockMvc.perform(delete("/story-saves/" + saveId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/story-saves")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
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

        String loginResponse = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(loginResponse).get("token").asText();
    }
}
