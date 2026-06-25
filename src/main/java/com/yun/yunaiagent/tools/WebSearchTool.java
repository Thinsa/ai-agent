package com.yun.yunaiagent.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yun.yunaiagent.constants.Constants;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
/**
 * Web 搜索工具。
 *
 * <p>封装 SearchAPI 查询与结果摘要格式化逻辑，让 Agent 可以获取实时网页信息。</p>
 */
public class WebSearchTool implements AgentTool {

    private static final int MAX_RESULTS = Constants.WEB_SEARCH_MAX_RESULTS;

    private final String apiKey;

    private final SearchApiClient searchApiClient;

    private final ObjectMapper objectMapper;

    @Autowired
    public WebSearchTool(@Value("${search-api.api-key:}") String apiKey) {
        this(apiKey, new RestClientSearchApiClient(RestClient.create()));
    }

    WebSearchTool(String apiKey, SearchApiClient searchApiClient) {
        this.apiKey = apiKey;
        this.searchApiClient = searchApiClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String name() {
        return "searchWeb";
    }

    @Override
    public String description() {
        return "通过 SearchAPI 搜索网页并返回摘要";
    }

    @Tool(description = "通过 SearchAPI 搜索网页，返回答案摘要、标题、链接和摘要片段")
    public String searchWeb(String query) {
        if (apiKey == null || apiKey.isBlank()) {
            return "搜索失败：未配置 SEARCH_API_API_KEY";
        }
        if (query == null || query.isBlank()) {
            return "搜索失败：搜索关键词不能为空";
        }
        try {
            String json = searchApiClient.search(query.trim(), apiKey);
            return formatSearchResult(json);
        } catch (Exception e) {
            return "搜索失败：" + e.getMessage();
        }
    }

    private String formatSearchResult(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json == null ? "{}" : json);
        List<String> lines = new ArrayList<>();
        lines.add("搜索摘要：");
        appendAnswerBox(lines, root.path("answer_box"));
        appendOrganicResults(lines, root.path("organic_results"));
        if (lines.size() == 1) {
            return "搜索无结果。";
        }
        return String.join(System.lineSeparator(), lines);
    }

    private void appendAnswerBox(List<String> lines, JsonNode answerBox) {
        if (!answerBox.isObject()) {
            return;
        }
        String answer = firstText(answerBox, "answer", "snippet", "title");
        if (!answer.isBlank()) {
            lines.add("答案：" + answer);
        }
    }

    private void appendOrganicResults(List<String> lines, JsonNode organicResults) {
        if (!organicResults.isArray()) {
            return;
        }
        int count = 0;
        for (JsonNode item : organicResults) {
            if (count >= MAX_RESULTS) {
                break;
            }
            String title = text(item, "title");
            String link = text(item, "link");
            String snippet = firstText(item, "snippet", "description");
            if (title.isBlank() && link.isBlank() && snippet.isBlank()) {
                continue;
            }
            count++;
            lines.add(count + ". " + fallback(title, "未命名结果"));
            if (!link.isBlank()) {
                lines.add("链接：" + link);
            }
            if (!snippet.isBlank()) {
                lines.add("摘要：" + snippet);
            }
        }
    }

    private String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = text(node, field);
            if (!value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isTextual() ? value.asText().trim() : "";
    }

    private String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    @FunctionalInterface
    /**
     * 搜索 API 客户端抽象。
     */
    interface SearchApiClient {
        String search(String query, String apiKey);
    }

    /**
     * 基于 RestClient 的 SearchAPI 客户端实现。
     */
    private static class RestClientSearchApiClient implements SearchApiClient {

        private final RestClient restClient;

        private RestClientSearchApiClient(RestClient restClient) {
            this.restClient = restClient;
        }

        @Override
        public String search(String query, String apiKey) {
            return restClient.get()
                    .uri("https://www.searchapi.io/api/v1/search?engine=baidu&q={query}&api_key={apiKey}", query, apiKey)
                    .retrieve()
                    .body(String.class);
        }
    }
}
