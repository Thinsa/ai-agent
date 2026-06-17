package com.yun.yunaiagent.rag;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 恋爱应用知识库文档加载器。
 *
 * <p>后续可从 classpath、数据库或对象存储读取原始文档，再交给切片器处理。</p>
 */
@Component
public class LoveAppDocumentLoader {

    /**
     * 加载待进入 RAG 流程的原始文档。
     */
    public List<String> loadDocuments() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:document/*.md");
            List<String> documents = new ArrayList<>();
            for (Resource resource : resources) {
                documents.add(resource.getContentAsString(StandardCharsets.UTF_8));
            }
            if (documents.isEmpty()) {
                return List.of("暂无知识库文档。");
            }
            return documents;
        } catch (Exception e) {
            return List.of("RAG 文档加载失败：" + e.getMessage());
        }
    }
}
