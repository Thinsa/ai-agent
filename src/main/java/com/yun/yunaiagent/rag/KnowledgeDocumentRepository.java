package com.yun.yunaiagent.rag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    List<KnowledgeDocument> findByEnabledTrue();

    List<KnowledgeDocument> findByEnabledTrueAndCategory(String category);

    long countByEnabledTrue();
}
