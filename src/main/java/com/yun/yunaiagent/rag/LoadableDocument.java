package com.yun.yunaiagent.rag;

/**
 * 文档加载器返回的原始文档中间表示。
 *
 * @param docId    文档唯一标识（如 "classpath:0" 或 "custom:42"）
 * @param title    文档标题
 * @param content  文档正文（markdown 文本）
 * @param source   来源类型（"classpath" | "custom"）
 * @param category 文档分类（"恋爱" | "婚姻" | "单身" | "自定义" 等）
 */
record LoadableDocument(String docId, String title, String content, String source, String category) {

    LoadableDocument {
        if (docId == null || docId.isBlank()) throw new IllegalArgumentException("docId must not be blank");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("content must not be blank");
    }
}
