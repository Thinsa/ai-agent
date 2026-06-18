package com.yun.yunaiagent.user;

import com.yun.yunaiagent.common.ValidationUtils;
import com.yun.yunaiagent.tools.OssObjectStorageService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@EnableConfigurationProperties(AliyunOssProperties.class)
public class AliyunOssAvatarStorageService implements AvatarStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final AliyunOssProperties properties;

    private final OssObjectStorageService objectStorageService;

    public AliyunOssAvatarStorageService(AliyunOssProperties properties, OssObjectStorageService objectStorageService) {
        this.properties = properties;
        this.objectStorageService = objectStorageService;
    }

    @Override
    public String uploadAvatar(String username, MultipartFile file) {
        validateFile(file);
        String objectKey = buildObjectKey(username, file.getOriginalFilename());
        try {
            return objectStorageService.upload(objectKey, file.getBytes(), file.getContentType());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read avatar file");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file is required");
        }
        long maxSize = properties.maxAvatarSizeBytes() <= 0 ? 5 * 1024 * 1024 : properties.maxAvatarSizeBytes();
        if (file.getSize() > maxSize) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file is too large");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only jpg, png, webp and gif avatars are supported");
        }
    }

    private String buildObjectKey(String username, String originalFilename) {
        String extension = extensionOf(originalFilename);
        String avatarDir = ValidationUtils.isBlank(properties.avatarDir()) ? "avatars" : trimSlashes(properties.avatarDir());
        String safeUsername = username.replaceAll("[^a-zA-Z0-9._-]", "_");
        return avatarDir + "/" + safeUsername + "/" + UUID.randomUUID() + extension;
    }

    private String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int index = filename.lastIndexOf('.');
        if (index < 0) {
            return "";
        }
        String extension = filename.substring(index).toLowerCase(Locale.ROOT);
        return extension.length() > 12 ? "" : extension;
    }

    private String trimSlashes(String value) {
        return value.replaceAll("^/+", "").replaceAll("/+$", "");
    }
}
