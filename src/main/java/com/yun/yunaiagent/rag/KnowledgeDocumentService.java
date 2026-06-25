package com.yun.yunaiagent.rag;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 自定义知识库文档的 CRUD 与重索引服务。
 */
@Service
/**
 * 知识库文档业务服务。
 *
 * <p>负责文档 CRUD、分类统计和索引重建编排，连接管理接口与向量检索服务。</p>
 */
public class KnowledgeDocumentService {

    private final KnowledgeDocumentRepository repository;

    private final LoveAppRagService ragService;

    public KnowledgeDocumentService(KnowledgeDocumentRepository repository, LoveAppRagService ragService) {
        this.repository = repository;
        this.ragService = ragService;
    }

    @Transactional(readOnly = true)
    public List<KnowledgeDocumentDto> listAll(boolean enabledOnly) {
        List<KnowledgeDocument> docs = enabledOnly
                ? repository.findByEnabledTrue()
                : repository.findAll();
        return docs.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public DocumentCountDto getCount() {
        long total = repository.count();
        long enabled = repository.countByEnabledTrue();
        return new DocumentCountDto(total, enabled);
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return List.of("恋爱", "婚姻", "单身", "自定义");
    }

    @Transactional
    public KnowledgeDocumentDto create(CreateDocumentRequest req) {
        KnowledgeDocument doc = KnowledgeDocument.create(req.title, req.content, req.category);
        doc = repository.save(doc);
        ragService.reindexAll();
        return toDto(doc);
    }

    @Transactional
    public KnowledgeDocumentDto update(Long id, UpdateDocumentRequest req) {
        KnowledgeDocument doc = repository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Document not found"));
        doc.update(req.title, req.content, req.category, req.enabled);
        doc = repository.save(doc);
        ragService.reindexAll();
        return toDto(doc);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Document not found");
        }
        repository.deleteById(id);
        ragService.reindexAll();
    }

    @Transactional
    public ReindexResultDto reindex() {
        ragService.reindexAll();
        long count = repository.countByEnabledTrue();
        return new ReindexResultDto(count);
    }

    private KnowledgeDocumentDto toDto(KnowledgeDocument doc) {
        return new KnowledgeDocumentDto(
                doc.getId(),
                doc.getTitle(),
                doc.getContent(),
                doc.getCategory(),
                doc.isEnabled(),
                doc.getSource(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }

    /**
     * 知识库文档展示数据。
     */
    public record KnowledgeDocumentDto(
            Long id,
            String title,
            String content,
            String category,
            boolean enabled,
            String source,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt
    ) {}

    /**
     * 创建知识库文档请求。
     */
    public record CreateDocumentRequest(String title, String content, String category) {}

    /**
     * 更新知识库文档请求。
     */
    public record UpdateDocumentRequest(String title, String content, String category, boolean enabled) {}

    /**
     * 知识库文档数量统计。
     */
    public record DocumentCountDto(long totalCount, long enabledCount) {}

    /**
     * 重建索引结果。
     */
    public record ReindexResultDto(long indexedCount) {}
}
