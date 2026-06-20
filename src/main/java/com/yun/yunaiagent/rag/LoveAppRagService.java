package com.yun.yunaiagent.rag;

import com.yun.yunaiagent.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 恋爱应用 RAG 服务。
 *
 * <p>支持双源文档加载：classpath 静态文档 + 数据库自定义文档。
 * 启动时自动索引，CRUD 操作后可通过 {@link #reindexAll()} 触发重索引。</p>
 */
@Component
public class LoveAppRagService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LoveAppRagService.class);

    private final LoveAppDocumentLoader documentLoader;

    private final QueryRewriter queryRewriter;

    private final ObjectProvider<VectorStore> vectorStoreProvider;

    private final KnowledgeDocumentRepository documentRepository;

    public LoveAppRagService(LoveAppDocumentLoader documentLoader, QueryRewriter queryRewriter,
                             ObjectProvider<VectorStore> vectorStoreProvider,
                             KnowledgeDocumentRepository documentRepository) {
        this.documentLoader = documentLoader;
        this.queryRewriter = queryRewriter;
        this.vectorStoreProvider = vectorStoreProvider;
        this.documentRepository = documentRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore == null) {
            log.info("LoveApp RAG vector store is not available; classpath documents will be used as fallback context.");
            return;
        }
        try {
            List<Document> documents = loadAllDocumentsForIndex();
            if (!documents.isEmpty()) {
                vectorStore.add(documents);
                log.info("Loaded {} LoveApp RAG documents into vector store.", documents.size());
            }
        } catch (Exception e) {
            log.warn("LoveApp RAG document indexing skipped: {}", e.getMessage());
        }
    }

    /**
     * 全量重索引：删除现有向量后重新加载 classpath + 启用的自定义文档。
     */
    public void reindexAll() {
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore == null) {
            log.warn("Cannot reindex: vector store is not available.");
            return;
        }
        try {
            // 删除所有现有向量（Spring AI PGVector 不支持按条件删除，所以全删）
            // 生产环境可优化为按 source 批量删除
            List<Document> classpathDocs = toVectorDocuments(documentLoader.loadDocuments());
            List<String> ids = classpathDocs.stream()
                    .map(Document::getId)
                    .toList();
            if (!ids.isEmpty()) {
                vectorStore.delete(ids);
            }
            // 重新加载并索引
            List<Document> allDocs = loadAllDocumentsForIndex();
            if (!allDocs.isEmpty()) {
                vectorStore.add(allDocs);
                log.info("Reindexed {} LoveApp RAG documents.", allDocs.size());
            }
        } catch (Exception e) {
            log.warn("LoveApp RAG reindex failed: {}", e.getMessage());
        }
    }

    public String retrieveContext(String query) {
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore == null) {
            return loadFallbackDocuments();
        }
        try {
            String rewrittenQuery = queryRewriter.rewrite(query);
            SearchRequest request = SearchRequest.builder()
                    .query(rewrittenQuery)
                    .topK(Constants.RAG_TOP_K)
                    .similarityThreshold(Constants.RAG_SIMILARITY_THRESHOLD)
                    .build();
            List<Document> matches = vectorStore.similaritySearch(request);
            if (matches == null || matches.isEmpty()) {
                return loadFallbackDocuments();
            }
            return matches.stream()
                    .map(Document::getText)
                    .reduce((left, right) -> left + System.lineSeparator() + right)
                    .orElse("");
        } catch (Exception e) {
            return "RAG retrieval failed: " + e.getMessage() + System.lineSeparator()
                    + loadFallbackDocuments();
        }
    }

    /**
     * 合并 classpath + 启用的自定义文档，准备索引。
     */
    private List<Document> loadAllDocumentsForIndex() {
        List<LoadableDocument> classpathDocs = documentLoader.loadDocuments();
        List<KnowledgeDocument> customDocs = documentRepository.findByEnabledTrue();

        List<LoadableDocument> all = new ArrayList<>(classpathDocs);
        for (KnowledgeDocument doc : customDocs) {
            all.add(new LoadableDocument(
                    "custom:" + doc.getId(),
                    doc.getTitle(),
                    doc.getContent(),
                    "custom",
                    doc.getCategory()
            ));
        }
        return toVectorDocuments(all);
    }

    /**
     * 构造降级回退的文档文本（VectorStore 不可用时使用）。
     */
    private String loadFallbackDocuments() {
        List<LoadableDocument> classpathDocs = documentLoader.loadDocuments();
        List<KnowledgeDocument> customDocs = documentRepository.findByEnabledTrue();

        StringBuilder sb = new StringBuilder();
        for (LoadableDocument doc : classpathDocs) {
            sb.append(doc.content()).append(System.lineSeparator());
        }
        for (KnowledgeDocument doc : customDocs) {
            sb.append(doc.getContent()).append(System.lineSeparator());
        }
        return sb.toString().trim();
    }

    /**
     * 按段落+字符数手工分块，确保每块不超过 embedding 模型上限（2048 tokens）。
     * 中文约 1-2 字符/token，取保守值 1500 字符/块，在段落边界处切分。
     */
    private List<Document> toVectorDocuments(List<LoadableDocument> sources) {
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < sources.size(); i++) {
            LoadableDocument src = sources.get(i);
            if (src.content() == null || src.content().isBlank()) {
                continue;
            }
            List<String> chunks = splitByParagraphs(src.content(), 1500);
            for (int j = 0; j < chunks.size(); j++) {
                Map<String, Object> metadata = new LinkedHashMap<>();
                metadata.put("source", src.source());
                metadata.put("index", i);
                metadata.put("chunk", j);
                metadata.put("title", src.title());
                metadata.put("category", src.category());
                if (src.docId() != null) {
                    metadata.put("docId", src.docId());
                }
                String logicalChunkId = src.docId() != null
                        ? src.docId() + "-chunk-" + j
                        : "love-doc-" + i + "-chunk-" + j;
                documents.add(new Document(stableVectorId(logicalChunkId), chunks.get(j), metadata));
            }
        }
        return documents;
    }

    /**
     * PGVector expects vector document IDs to be UUID strings.
     * Keep source doc IDs in metadata, and derive stable UUIDs for vector rows.
     */
    private String stableVectorId(String logicalChunkId) {
        return UUID.nameUUIDFromBytes(("love-rag:" + logicalChunkId).getBytes(StandardCharsets.UTF_8))
                .toString();
    }

    /**
     * 在段落边界（#### / 空行）处将文本切分为长度不超过 maxChars 的块。
     */
    private List<String> splitByParagraphs(String text, int maxChars) {
        List<String> result = new ArrayList<>();
        // 按 #### 标题或双换行分割段落
        String[] paragraphs = text.split("(?=\\n####\\s)|\\n\\n+");
        StringBuilder current = new StringBuilder();
        for (String para : paragraphs) {
            para = para.trim();
            if (para.isEmpty()) continue;
            // 如果当前块加上这个段落超出限制，先存当前块
            if (current.length() + para.length() + 1 > maxChars && current.length() > 0) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            }
            // 如果单个段落本身就超长，按句号/换行进一步拆分
            if (para.length() > maxChars) {
                // 先 flush 当前累积
                if (current.length() > 0) {
                    result.add(current.toString().trim());
                    current = new StringBuilder();
                }
                // 按句号拆分超长段落
                String[] sentences = para.split("(?<=[。！？])");
                for (String sentence : sentences) {
                    sentence = sentence.trim();
                    if (sentence.isEmpty()) continue;
                    if (current.length() + sentence.length() > maxChars && current.length() > 0) {
                        result.add(current.toString().trim());
                        current = new StringBuilder();
                    }
                    current.append(sentence);
                }
            } else {
                current.append(para).append("\n\n");
            }
        }
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }
        return result;
    }
}
