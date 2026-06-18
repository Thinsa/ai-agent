package com.yun.yunaiagent.user;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aliyun.oss")
public record AliyunOssProperties(
        String endpoint,
        String bucket,
        String accessKeyId,
        String accessKeySecret,
        String publicBaseUrl,
        String avatarDir,
        long maxAvatarSizeBytes
) {
}
