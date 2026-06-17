package com.yun.yunaiagent.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class LoveAppRagService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LoveAppRagService.class);

    private final LoveAppDocumentLoader documentLoader;

    private final QueryRewriter queryRewriter;

    private final ObjectProvider<VectorStore> vectorStoreProvider;

    public LoveAppRagService(LoveAppDocumentLoader documentLoader, QueryRewriter queryRewriter, ObjectProvider<VectorStore> vectorStoreProvider) {
        this.documentLoader = documentLoader;
        this.queryRewriter = queryRewriter;
        this.vectorStoreProvider = vectorStoreProvider;
    }

    @Override
    public void run(ApplicationArguments args) {
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore == null) {
            log.info("LoveApp RAG vector store is not available; classpath documents will be used as fallback context.");
            return;
        }
        try {
            List<Document> documents = toVectorDocuments(documentLoader.loadDocuments());
            if (!documents.isEmpty()) {
                vectorStore.add(documents);
                log.info("Loaded {} LoveApp RAG documents into vector store.", documents.size());
            }
        } catch (Exception e) {
            log.warn("LoveApp RAG document indexing skipped: {}", e.getMessage());
        }
    }

    public String retrieveContext(String query) {
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore == null) {
            return String.join(System.lineSeparator(), documentLoader.loadDocuments());
        }
        try {
            String rewrittenQuery = queryRewriter.rewrite(query);
            SearchRequest request = SearchRequest.builder()
                    .query(rewrittenQuery)
                    .topK(4)
                    .similarityThreshold(0.5)
                    .build();
            List<Document> matches = vectorStore.similaritySearch(request);
            if (matches == null || matches.isEmpty()) {
                return String.join(System.lineSeparator(), documentLoader.loadDocuments());
            }
            return matches.stream()
                    .map(Document::getText)
                    .reduce((left, right) -> left + System.lineSeparator() + right)
                    .orElse("");
        } catch (Exception e) {
            return "RAG retrieval failed: " + e.getMessage() + System.lineSeparator()
                    + String.join(System.lineSeparator(), documentLoader.loadDocuments());
        }
    }

    private List<Document> toVectorDocuments(List<String> rawDocuments) {
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < rawDocuments.size(); i++) {
            String content = rawDocuments.get(i);
            if (content == null || content.isBlank()) {
                continue;
            }
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("source", "classpath:document");
            metadata.put("index", i);
            documents.add(new Document("love-app-doc-" + i, content, metadata));
        }
        return documents;
    }
}
