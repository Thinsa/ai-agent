package com.yun.yunaiagent.rag;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 恋爱应用知识库文档加载器。
 *
 * <p>从 classpath 加载原始 .md 文档，返回带元数据的 {@link LoadableDocument}。
 * 后续可从数据库或对象存储补充自定义文档。</p>
 */
@Component
/**
 * 内置知识文档加载器。
 *
 * <p>从 resources/document 读取初始语料并转换为统一文档对象，用于应用启动时初始化 RAG 数据。</p>
 */
public class LoveAppDocumentLoader {

    /**
     * 文件名到分类的映射规则。
     * 匹配 "恋爱常见问题和回答 - 单身篇.md" → "单身"
     */
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("[（(]?([^）)篇]+)[篇)）]");

    /**
     * 保留的文件名前缀（用于提取标题）。
     */
    private static final Map<String, String> FILE_CATEGORY_MAP = Map.of(
            "love-guide", "恋爱",
            "单身篇", "单身",
            "恋爱篇", "恋爱",
            "已婚篇", "婚姻"
    );

    /**
     * 加载待进入 RAG 流程的原始文档。
     */
    public List<LoadableDocument> loadDocuments() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:document/*.md");
            List<LoadableDocument> documents = new ArrayList<>();
            for (int i = 0; i < resources.length; i++) {
                Resource resource = resources[i];
                String filename = resource.getFilename();
                String content = resource.getContentAsString(StandardCharsets.UTF_8);
                if (content == null || content.isBlank()) {
                    continue;
                }
                String title = extractTitle(filename);
                String category = extractCategory(filename);
                String docId = "classpath:" + i;
                documents.add(new LoadableDocument(docId, title, content, "classpath", category));
            }
            if (documents.isEmpty()) {
                return List.of(new LoadableDocument("classpath:fallback", "暂无知识库文档", "暂无知识库文档。", "classpath", "默认"));
            }
            return documents;
        } catch (Exception e) {
            return List.of(new LoadableDocument("classpath:error", "RAG 文档加载失败", "RAG 文档加载失败：" + e.getMessage(), "classpath", "默认"));
        }
    }

    /**
     * 从文件名提取可读标题。
     */
    private String extractTitle(String filename) {
        if (filename == null) return "未命名文档";
        // 去掉 .md 后缀
        String name = filename.replaceAll("\\.md$", "");
        // "恋爱常见问题和回答 - 单身篇" → "恋爱常见问题和回答"
        int dashIdx = name.indexOf(" - ");
        if (dashIdx > 0) {
            return name.substring(0, dashIdx).trim();
        }
        return name;
    }

    /**
     * 从文件名提取分类。
     */
    private String extractCategory(String filename) {
        if (filename == null) return "自定义";
        Matcher matcher = CATEGORY_PATTERN.matcher(filename);
        if (matcher.find()) {
            String key = matcher.group(1).trim();
            // 尝试匹配已知分类
            for (Map.Entry<String, String> entry : FILE_CATEGORY_MAP.entrySet()) {
                if (key.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
            return key;
        }
        // 尝试按文件名前缀匹配
        for (Map.Entry<String, String> entry : FILE_CATEGORY_MAP.entrySet()) {
            if (filename.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "自定义";
    }

}
