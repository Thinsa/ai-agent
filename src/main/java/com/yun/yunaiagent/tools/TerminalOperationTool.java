package com.yun.yunaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 终端命令执行工具占位实现。
 *
 * <p>真实执行命令风险较高，接入时应增加白名单、超时、工作目录限制和输出截断。</p>
 */
@ConditionalOnProperty(name = "app.tools.terminal-execution.enabled", havingValue = "true")
@Component
public class TerminalOperationTool implements AgentTool {

    private final int timeoutSeconds;

    private final int maxOutputChars;

    public TerminalOperationTool(
            @Value("${app.tools.command-timeout-seconds:30}") int timeoutSeconds,
            @Value("${app.tools.max-output-chars:12000}") int maxOutputChars
    ) {
        this.timeoutSeconds = timeoutSeconds;
        this.maxOutputChars = maxOutputChars;
    }

    @Override
    public String name() {
        return "executeTerminalCommand";
    }

    @Override
    public String description() {
        return "终端命令执行工具骨架";
    }

    /**
     * 执行终端命令的预留方法。
     */
    @Tool(description = "执行本地终端命令并返回输出")
    public String executeTerminalCommand(String command) {
        if (command == null || command.isBlank()) {
            return "终端命令失败：命令不能为空";
        }
        try {
            Process process = new ProcessBuilder(commandParts(command))
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "终端命令超时：" + Duration.ofSeconds(timeoutSeconds);
            }
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
                String line;
                while ((line = reader.readLine()) != null && output.length() < maxOutputChars) {
                    output.append(line).append(System.lineSeparator());
                }
            }
            return "exitCode=" + process.exitValue() + System.lineSeparator() + output.toString().trim();
        } catch (Exception e) {
            return "终端命令失败：" + e.getMessage();
        }
    }

    private List<String> commandParts(String command) {
        List<String> parts = new ArrayList<>();
        boolean inQuote = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < command.length(); i++) {
            char ch = command.charAt(i);
            if (ch == '"') {
                inQuote = !inQuote;
                continue;
            }
            if (Character.isWhitespace(ch) && !inQuote) {
                if (!current.isEmpty()) {
                    parts.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }
            current.append(ch);
        }
        if (!current.isEmpty()) {
            parts.add(current.toString());
        }
        return parts;
    }
}
