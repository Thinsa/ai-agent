package com.yun.yunaiagent.rag;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoveAppDocumentLoaderTest {

    @Test
    void loadDocumentsReadsClasspathMarkdownFiles() {
        LoveAppDocumentLoader loader = new LoveAppDocumentLoader();

        assertThat(loader.loadDocuments())
                .anySatisfy(document -> assertThat(document).contains("健康关系"));
    }
}
