package com.yun.yunaiagent.tools;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.yun.yunaiagent.user.AliyunOssProperties;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
@EnableConfigurationProperties(AliyunOssProperties.class)
public class OssObjectStorageService implements DisposableBean {

    private final AliyunOssProperties properties;

    private volatile OSS ossClient;

    public OssObjectStorageService(AliyunOssProperties properties) {
        this.properties = properties;
    }

    public String upload(String objectKey, byte[] content, String contentType) {
        validateConfig();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(content.length);
        metadata.setContentType(contentType);

        ossClient().putObject(properties.bucket(), objectKey, new ByteArrayInputStream(content), metadata);
        return publicBaseUrl() + "/" + objectKey;
    }

    /**
     * 懒加载获取 OSS 客户端单例，通过双重检查锁保证线程安全。
     * 阿里云 OSS SDK 客户端是线程安全的，可以跨请求复用。
     */
    private OSS ossClient() {
        if (ossClient == null) {
            synchronized (this) {
                if (ossClient == null) {
                    ossClient = new OSSClientBuilder().build(
                            properties.endpoint(),
                            properties.accessKeyId(),
                            properties.accessKeySecret()
                    );
                }
            }
        }
        return ossClient;
    }

    @Override
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    private void validateConfig() {
        if (isBlank(properties.endpoint()) || isBlank(properties.bucket())
                || isBlank(properties.accessKeyId()) || isBlank(properties.accessKeySecret())) {
            throw new IllegalStateException("Aliyun OSS is not configured");
        }
    }

    private String publicBaseUrl() {
        if (!isBlank(properties.publicBaseUrl())) {
            return trimRightSlash(properties.publicBaseUrl());
        }
        String endpoint = properties.endpoint()
                .replace("https://", "")
                .replace("http://", "");
        return "https://" + properties.bucket() + "." + trimRightSlash(endpoint);
    }

    private String trimRightSlash(String value) {
        return value.replaceAll("/+$", "");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
