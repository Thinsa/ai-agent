package com.yun.yunaiagent.rag;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文档切片器。
 *
 * <p>当前直接返回原文；后续可按 token 数、段落或语义边界拆分长文档。</p>
 */
@Component
public class MyTokenTextSplitter {

    /**
     * 将单篇文档拆成适合向量化和检索的片段。
     */
    public List<String> split(String document) {
        return List.of(document);
    }
}
