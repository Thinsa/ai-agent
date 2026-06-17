package com.yun.yunaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Web 搜索工具占位实现。
 *
 * <p>后续可使用配置中的 SearchAPI 密钥调用真实搜索服务。</p>
 */
@Component
public class WebSearchTool implements AgentTool {

    /**
     * 搜索服务密钥，从 application.yml 的 search-api.api-key 读取。
     */
    @Value("${search-api.api-key:}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();

    @Override
    public String name() {
        return "searchWeb";
    }

    @Override
    public String description() {
        return "通过 SearchAPI 搜索网页的工具骨架";
    }

    /**
     * 根据关键词搜索网页的预留方法。
     */
    @Tool(description = "通过 SearchAPI 搜索网页")
    public String searchWeb(String query) {
        if (apiKey == null || apiKey.isBlank()) {
            return "搜索失败：未配置 search-api.api-key";
        }
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            return restClient.get()
                    .uri("https://www.searchapi.io/api/v1/search?engine=baidu&q={query}&api_key={apiKey}", encodedQuery, apiKey)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            return "搜索失败：" + e.getMessage();
        }
    }
}
