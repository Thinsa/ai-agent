package com.yun.yunaiagent.rag;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoveAppDocumentLoaderTest {

    @Test
    void loadDocumentsReadsClasspathMarkdownFiles() {
        LoveAppDocumentLoader loader = new LoveAppDocumentLoader();

        var documents = loader.loadDocuments();

        assertThat(documents)
                .hasSizeGreaterThanOrEqualTo(4)
                .anySatisfy(document -> assertThat(document).contains("健康关系"));
        assertThat(documents).anySatisfy(document -> assertThat(document).contains("单身篇"));
        assertThat(documents).anySatisfy(document -> assertThat(document).contains("已婚篇"));
        assertThat(documents).anySatisfy(document -> assertThat(document).contains("恋爱篇"));
    }
}
