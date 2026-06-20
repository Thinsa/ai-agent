package com.yun.yunaiagent.controller;

import com.yun.yunaiagent.chat.ChatHistoryService;
import com.yun.yunaiagent.security.JwtService;
import com.yun.yunaiagent.service.StreamingChatService;
import com.yun.yunaiagent.tools.OssObjectStorageService;
import com.yun.yunaiagent.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatControllerUploadTest {

    @Test
    void imageUploadReturnsImageUrlForVisionChat() {
        OssObjectStorageService ossService = mock(OssObjectStorageService.class);
        when(ossService.upload(any(), any(), eq("image/png")))
                .thenReturn("https://cdn.example.com/uploads/images/pokemo.png");
        ChatController controller = new ChatController(
                mock(ChatModel.class),
                mock(ChatHistoryService.class),
                mock(StreamingChatService.class),
                ossService,
                mock(UserService.class),
                mock(JwtService.class)
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "pokemo.png",
                "image/png",
                new byte[]{(byte) 0x89, 'P', 'N', 'G'}
        );

        Map<String, String> result = controller.uploadFile(file, null, null);

        assertThat(result)
                .containsEntry("fileName", "pokemo.png")
                .containsEntry("contentType", "image/png")
                .containsEntry("imageUrl", "https://cdn.example.com/uploads/images/pokemo.png")
                .containsEntry("isImage", "true");
    }
}
