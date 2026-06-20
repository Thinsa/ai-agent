package com.yun.yunaiagent.rag;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * 空的 KnowledgeDocumentRepository 实现，用于无需 DB 数据的单元测试。
 */
public class EmptyKnowledgeDocumentRepository implements KnowledgeDocumentRepository {

    @Override
    public List<KnowledgeDocument> findByEnabledTrue() { return List.of(); }

    @Override
    public List<KnowledgeDocument> findByEnabledTrueAndCategory(String category) { return List.of(); }

    @Override
    public long countByEnabledTrue() { return 0; }

    // JpaRepository stubs
    @Override public List<KnowledgeDocument> findAll() { return List.of(); }
    @Override public List<KnowledgeDocument> findAll(Sort sort) { return List.of(); }
    @Override public Page<KnowledgeDocument> findAll(Pageable pageable) { return Page.empty(); }
    @Override public List<KnowledgeDocument> findAllById(Iterable<Long> ids) { return List.of(); }
    @Override public long count() { return 0; }
    @Override public void deleteById(Long id) {}
    @Override public void delete(KnowledgeDocument entity) {}
    @Override public void deleteAllById(Iterable<? extends Long> ids) {}
    @Override public void deleteAll(Iterable<? extends KnowledgeDocument> entities) {}
    @Override public void deleteAll() {}
    @Override public <S extends KnowledgeDocument> S save(S entity) { return entity; }
    @Override public <S extends KnowledgeDocument> List<S> saveAll(Iterable<S> entities) { return List.of(); }
    @Override public Optional<KnowledgeDocument> findById(Long id) { return Optional.empty(); }
    @Override public boolean existsById(Long id) { return false; }
    @Override public void flush() {}
    @Override public <S extends KnowledgeDocument> S saveAndFlush(S entity) { return entity; }
    @Override public <S extends KnowledgeDocument> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
    @Override public void deleteAllInBatch(Iterable<KnowledgeDocument> entities) {}
    @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
    @Override public void deleteAllInBatch() {}
    @Override public KnowledgeDocument getOne(Long id) { return null; }
    @Override public KnowledgeDocument getById(Long id) { return null; }
    @Override public KnowledgeDocument getReferenceById(Long id) { return null; }
    @Override public <S extends KnowledgeDocument> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
    @Override public <S extends KnowledgeDocument> List<S> findAll(Example<S> example) { return List.of(); }
    @Override public <S extends KnowledgeDocument> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
    @Override public <S extends KnowledgeDocument> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
    @Override public <S extends KnowledgeDocument> long count(Example<S> example) { return 0; }
    @Override public <S extends KnowledgeDocument> boolean exists(Example<S> example) { return false; }
    @Override public <S extends KnowledgeDocument, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
}
