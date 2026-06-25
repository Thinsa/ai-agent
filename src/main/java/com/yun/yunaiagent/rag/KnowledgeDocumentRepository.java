package com.yun.yunaiagent.rag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 知识库文档数据访问接口。
 *
 * <p>提供按启用状态、分类等维度查询文档的能力，供管理端和索引重建流程调用。</p>
 */
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    List<KnowledgeDocument> findByEnabledTrue();

    List<KnowledgeDocument> findByEnabledTrueAndCategory(String category);

    long countByEnabledTrue();
}
