package com.yun.yunaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文件读写工具占位实现。
 *
 * <p>真正落地时需要重点处理路径白名单、编码、文件大小和异常信息脱敏。</p>
 */
@Component
public class FileOperationTool implements AgentTool {

    @Override
    public String name() {
        return "fileOperation";
    }

    @Override
    public String description() {
        return "读取或写入文件的工具骨架";
    }

    /**
     * 读取指定文件内容的预留方法。
     */
    @Tool(description = "读取指定路径的文本文件内容")
    public String readFile(String filePath) {
        try {
            return Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "文件读取失败：" + e.getMessage();
        }
    }

    /**
     * 写入指定文件内容的预留方法。
     */
    @Tool(description = "把文本内容写入指定路径的文件")
    public String writeFile(String filePath, String content) {
        try {
            Path path = Path.of(filePath);
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, content == null ? "" : content, StandardCharsets.UTF_8);
            return "文件写入成功：" + path;
        } catch (IOException e) {
            return "文件写入失败：" + e.getMessage();
        }
    }
}
