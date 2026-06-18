package com.yun.yunaiagent.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:user-controller-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
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
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AvatarStorageService avatarStorageService;

    @Test
    void registerLoginCurrentAndUpdateProfileUseJwtAuthentication() throws Exception {
        Map<String, String> registerRequest = Map.of(
                "username", "ash",
                "password", "pikachu123",
                "displayName", "小智",
                "email", "ash@example.com"
        );

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ash"))
                .andExpect(jsonPath("$.displayName").value("小智"));

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());

        String loginResponse = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("username", "ash", "password", "pikachu123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("ash"))
                .andExpect(jsonPath("$.expiresAt").isNumber())
                .andExpect(jsonPath("$.expiresIn").value(3600000))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();
        assertThat(token).isNotBlank();

        mockMvc.perform(get("/user/current"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/user/current")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ash"));

        mockMvc.perform(get("/user/token/validate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("ash"))
                .andExpect(jsonPath("$.expiresAt").isNumber());

        mockMvc.perform(get("/user/token/validate")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));

        mockMvc.perform(get("/user/token/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));

        mockMvc.perform(put("/user/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "displayName", "皮卡丘训练家",
                                "email", "trainer@example.com",
                                "role", "AI 应用体验官",
                                "bio", "正在联调真实后端"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("皮卡丘训练家"))
                .andExpect(jsonPath("$.email").value("trainer@example.com"));

        when(avatarStorageService.uploadAvatar(eq("ash"), any()))
                .thenReturn("https://yuc-ai.oss-cn-beijing.aliyuncs.com/avatars/ash/avatar.png");
        MockMultipartFile avatar = new MockMultipartFile(
                "file",
                "avatar.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/user/avatar")
                        .file(avatar)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ash"))
                .andExpect(jsonPath("$.avatarUrl").value("https://yuc-ai.oss-cn-beijing.aliyuncs.com/avatars/ash/avatar.png"));
    }
}
