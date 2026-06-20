package com.yun.yunaiagent.controller;

import com.yun.yunaiagent.chat.ChatHistoryService;
import com.yun.yunaiagent.common.SecurityUtils;
import com.yun.yunaiagent.common.ValidationUtils;
import com.yun.yunaiagent.security.JwtService;
import com.yun.yunaiagent.service.StreamingChatService;
import com.yun.yunaiagent.tools.OssObjectStorageService;
import com.yun.yunaiagent.user.UserService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatModel chatModel;
    private final ChatHistoryService chatHistoryService;
    private final StreamingChatService streamingChatService;
    private final OssObjectStorageService ossService;
    private final UserService userService;
    private final JwtService jwtService;

    public ChatController(
            @Qualifier("openAiChatModel") ChatModel chatModel,
            ChatHistoryService chatHistoryService,
            StreamingChatService streamingChatService,
            OssObjectStorageService ossService,
            UserService userService,
            JwtService jwtService) {
        this.chatModel = chatModel;
        this.chatHistoryService = chatHistoryService;
        this.streamingChatService = streamingChatService;
        this.ossService = ossService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping(value = "/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatBasic(String message, String chatId,
            @RequestParam(required = false) String token, Authentication authentication) {
        var user = SecurityUtils.currentUser(authentication, token, jwtService, userService);
        return streamingChatService.streamAndPersist("chat", chatId, message, user, chatHistoryService, () ->
                ChatClient.builder(chatModel).build().prompt()
                        .system("你是灵语 Spark，LinkMind 灵桥的日常对话伙伴。请用友好、自然的中文回答用户的问题，保持回答简洁有温度。")
                        .user(ValidationUtils.normalize(message, "空消息"))
                        .stream()
                        .content());
    }

    @PostMapping("/upload")
    public Map<String, String> uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String token, Authentication authentication) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        try {
            SecurityUtils.currentUser(authentication, token, jwtService, userService);

            String originalName = file.getOriginalFilename();
            String safeName = (originalName != null && !originalName.isBlank())
                    ? originalName : "upload_" + System.currentTimeMillis();
            String contentType = file.getContentType();
            boolean isImage = contentType != null && contentType.startsWith("image/");

            if (isImage) {
                // Image upload to OSS for public URL
                String objectKey = "uploads/images/" + System.currentTimeMillis() + "_" + safeName;
                String imageUrl = ossService.upload(objectKey, file.getBytes(), contentType);
                return Map.of(
                        "fileName", safeName,
                        "filePath", objectKey,
                        "contentType", contentType,
                        "imageUrl", imageUrl,
                        "preview", "",
                        "isImage", "true"
                );
            }

            // Non-image files saved to local workspace
            Path workspaceDir = Path.of(System.getProperty("user.dir"), "workspace", "uploads");
            Files.createDirectories(workspaceDir);
            Path target = workspaceDir.resolve(safeName);
            file.transferTo(target);

            String preview = "";
            if (contentType != null && (contentType.startsWith("text/") ||
                    safeName.matches(".*\\.(txt|md|json|xml|csv|log|java|py|js|html|css|yml|yaml|properties)$"))) {
                try {
                    String content = Files.readString(target);
                    preview = content.length() > 2000 ? content.substring(0, 2000) + "\n...(truncated)" : content;
                } catch (IOException ignored) {
                }
            }

            return Map.of(
                    "fileName", safeName,
                    "filePath", target.toString(),
                    "contentType", contentType != null ? contentType : "application/octet-stream",
                    "preview", preview,
                    "isImage", "false"
            );
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed: " + e.getMessage(), e);
        }
    }
}
