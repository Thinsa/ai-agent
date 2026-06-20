package com.yun.yunaiagent.background;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yun.yunaiagent.tools.OssObjectStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.yun.yunaiagent.common.TestAuthHelper.registerAndLogin;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:background-controller-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
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
class BackgroundControllerTest {

    private static final byte[] PNG_BYTES = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    OssObjectStorageService ossObjectStorageService;

    @Test
    void savesBackgroundViaMultipartAndReadsItBack() throws Exception {
        String token = registerAndLogin(mockMvc, objectMapper,"bg-user-mp");
        when(ossObjectStorageService.upload(any(), any(), eq("image/png")))
                .thenReturn("https://yuc-ai.oss-cn-beijing.aliyuncs.com/backgrounds/bg-user-mp/love/bg.png");

        MockMultipartFile file = new MockMultipartFile(
                "file", "bg.png", "image/png", PNG_BYTES);

        mockMvc.perform(multipart("/backgrounds/love")
                        .file(file)
                        .param("opacity", "0.3")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agentKey").value("love"))
                .andExpect(jsonPath("$.imageData").value("https://yuc-ai.oss-cn-beijing.aliyuncs.com/backgrounds/bg-user-mp/love/bg.png"))
                .andExpect(jsonPath("$.opacity").value(0.3));

        mockMvc.perform(get("/backgrounds")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.love.imageData").value("https://yuc-ai.oss-cn-beijing.aliyuncs.com/backgrounds/bg-user-mp/love/bg.png"))
                .andExpect(jsonPath("$.love.opacity").value(0.3));
    }

    @Test
    void updatesOpacityViaJson() throws Exception {
        String token = registerAndLogin(mockMvc, objectMapper,"opacity-user-mp");
        when(ossObjectStorageService.upload(any(), any(), eq("image/png")))
                .thenReturn("https://yuc-ai.oss-cn-beijing.aliyuncs.com/backgrounds/opacity-user-mp/super/bg.png");

        MockMultipartFile file = new MockMultipartFile(
                "file", "bg.png", "image/png", PNG_BYTES);

        mockMvc.perform(multipart("/backgrounds/super")
                        .file(file)
                        .param("opacity", "0.2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/backgrounds/super/opacity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"opacity\":0.6}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agentKey").value("super"))
                .andExpect(jsonPath("$.opacity").value(0.6));
    }

    @Test
    void deletesBackground() throws Exception {
        String token = registerAndLogin(mockMvc, objectMapper,"delete-user-mp");
        when(ossObjectStorageService.upload(any(), any(), eq("image/png")))
                .thenReturn("https://yuc-ai.oss-cn-beijing.aliyuncs.com/backgrounds/delete-user-mp/love/bg.png");

        MockMultipartFile file = new MockMultipartFile(
                "file", "bg.png", "image/png", PNG_BYTES);

        mockMvc.perform(multipart("/backgrounds/love")
                        .file(file)
                        .param("opacity", "0.3")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/backgrounds/love")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/backgrounds")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.love").doesNotExist());
    }

    @Test
    void rejectsEmptyFile() throws Exception {
        String token = registerAndLogin(mockMvc, objectMapper,"empty-file-user");
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[0]);

        mockMvc.perform(multipart("/backgrounds/love")
                        .file(emptyFile)
                        .param("opacity", "0.3")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
}
