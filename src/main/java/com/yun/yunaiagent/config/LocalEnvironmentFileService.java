package com.yun.yunaiagent.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

/**
 * 合并本地 txt 配置与环境变量，统一维护本地敏感配置清单。
 */
public class LocalEnvironmentFileService {

    public static final List<String> MANAGED_KEYS = List.of(
            "DASHSCOPE_API_KEY",
            "DASHSCOPE_CHAT_MODEL",
            "OPENAI_API_KEY",
            "OPENAI_BASE_URL",
            "OPENAI_CHAT_MODEL",
            "OPENAI_EMBEDDING_MODEL",
            "SEARCH_API_API_KEY",
            "SPRING_DATASOURCE_URL",
            "SPRING_DATASOURCE_USERNAME",
            "SPRING_DATASOURCE_PASSWORD",
            "APP_JWT_SECRET",
            "APP_JWT_EXPIRATION",
            "APP_TOOLS_WORKSPACE",
            "APP_TOOLS_COMMAND_TIMEOUT_SECONDS",
            "APP_TOOLS_MAX_OUTPUT_CHARS",
            "APP_MCP_CLIENT_ENABLED",
            "APP_MCP_IMAGE_SEARCH_URL",
            "PEXELS_API_KEY",
            "PEXELS_API_URL"
    );

    public MergeResult merge(Path envFile, Map<String, String> userEnvironment, Map<String, String> processEnvironment) throws IOException {
        LinkedHashMap<String, String> merged = new LinkedHashMap<>();
        if (Files.exists(envFile)) {
            for (String line : Files.readAllLines(envFile, StandardCharsets.UTF_8)) {
                int separatorIndex = line.indexOf('=');
                if (separatorIndex <= 0) {
                    continue;
                }
                String key = normalizeKey(line.substring(0, separatorIndex));
                String value = line.substring(separatorIndex + 1);
                if (!key.isBlank()) {
                    merged.put(key, value);
                }
            }
        }

        mergeEnvironment(merged, userEnvironment);
        mergeEnvironment(merged, processEnvironment);

        TreeSet<String> orderedKeys = new TreeSet<>(merged.keySet());
        List<String> lines = new ArrayList<>();
        for (String key : orderedKeys) {
            lines.add(key + "=" + Objects.toString(merged.get(key), ""));
        }
        Files.createDirectories(envFile.toAbsolutePath().getParent());
        Files.write(envFile, lines, StandardCharsets.UTF_8);

        List<String> writtenKeys = new ArrayList<>(orderedKeys);
        return new MergeResult(writtenKeys, safeSummary(merged));
    }

    private void mergeEnvironment(Map<String, String> merged, Map<String, String> environment) {
        for (String key : MANAGED_KEYS) {
            String value = environment.get(key);
            if (value != null && !value.isBlank()) {
                merged.put(key, value);
            }
        }
    }

    private String normalizeKey(String rawKey) {
        if (rawKey == null) {
            return "";
        }
        String key = rawKey.replace("\uFEFF", "").trim();
        for (String managedKey : MANAGED_KEYS) {
            if (key.endsWith(managedKey)) {
                return managedKey;
            }
            if (managedKey.length() > 1 && key.endsWith(managedKey.substring(1))) {
                return managedKey;
            }
        }
        int start = 0;
        while (start < key.length()) {
            char ch = key.charAt(start);
            if ((ch >= 'A' && ch <= 'Z') || ch == '_') {
                break;
            }
            start++;
        }
        return start == 0 ? key : key.substring(start);
    }

    private String safeSummary(Map<String, String> merged) {
        List<String> parts = new ArrayList<>();
        for (String key : new TreeSet<>(merged.keySet())) {
            parts.add(key + "=" + (merged.get(key) == null || merged.get(key).isBlank() ? "empty" : "set"));
        }
        return String.join(System.lineSeparator(), parts);
    }

    public record MergeResult(List<String> writtenKeys, String safeSummary) {
    }
}
