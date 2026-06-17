package com.yun.yunaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 远程资源下载工具占位实现。
 *
 * <p>真正下载前应校验 URL 协议、文件大小、目标路径和网络超时。</p>
 */
@Component
public class ResourceDownloadTool implements AgentTool {

    @Override
    public String name() {
        return "downloadResource";
    }

    @Override
    public String description() {
        return "资源下载工具骨架";
    }

    /**
     * 下载远程资源到本地目标路径的预留方法。
     */
    @Tool(description = "下载远程 URL 资源到本地文件")
    public String downloadResource(String url, String targetPath) {
        try (InputStream inputStream = URI.create(url).toURL().openStream()) {
            Path path = Path.of(targetPath);
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.copy(inputStream, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return "资源下载成功：" + url + " -> " + path;
        } catch (Exception e) {
            return "资源下载失败：" + e.getMessage();
        }
    }
}
