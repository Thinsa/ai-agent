package com.yun.yunaiagent.rag;

import org.springframework.stereotype.Component;

/**
 * 文档关键词增强器。
 *
 * <p>后续可为文档片段补充主题词、标签或摘要，提高检索召回质量。</p>
 */
@Component
public class MyKeywordEnricher {

    /**
     * 对文档内容做关键词增强。
     */
    public String enrich(String document) {
        return document;
    }
}
