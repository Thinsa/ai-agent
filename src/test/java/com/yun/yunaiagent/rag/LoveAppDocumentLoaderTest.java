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
                .anySatisfy(doc -> assertThat(doc.content()).contains("健康关系"));
        assertThat(documents).anySatisfy(doc -> assertThat(doc.content()).contains("单身篇"));
        assertThat(documents).anySatisfy(doc -> assertThat(doc.content()).contains("已婚篇"));
        assertThat(documents).anySatisfy(doc -> assertThat(doc.content()).contains("恋爱篇"));
    }

    @Test
    void loadDocumentsReturnsLoadableDocumentsWithMetadata() {
        LoveAppDocumentLoader loader = new LoveAppDocumentLoader();

        var documents = loader.loadDocuments();

        assertThat(documents).allSatisfy(doc -> {
            assertThat(doc.docId()).startsWith("classpath:");
            assertThat(doc.source()).isEqualTo("classpath");
            assertThat(doc.title()).isNotBlank();
            assertThat(doc.category()).isNotBlank();
            assertThat(doc.content()).isNotBlank();
        });
    }
}
