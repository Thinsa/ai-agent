package com.yun.yunaiagent.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationYamlTest {

    @Test
    void mcpClientIsDisabledByDefault() throws Exception {
        String yaml = Files.readString(Path.of("src/main/resources/application.yml"));

        assertThat(yaml).contains("enabled: ${APP_MCP_CLIENT_ENABLED:false}");
    }

    @Test
    void committedProfileDoesNotContainHardcodedApiKeys() throws Exception {
        String yaml = Files.readString(Path.of("src/main/resources/application.yml"));

        assertThat(yaml).contains("${DASHSCOPE_API_KEY:}");
        assertThat(yaml).contains("${OPENAI_API_KEY:${DASHSCOPE_API_KEY:}}");
        assertThat(yaml).contains("model: ${DASHSCOPE_IMAGE_MODEL:qwen-image-2.0-pro}");
        assertThat(yaml).contains("size: ${DASHSCOPE_IMAGE_SIZE:2048*2048}");
        assertThat(yaml).contains("negative-prompt: ${DASHSCOPE_IMAGE_NEGATIVE_PROMPT:}");
        assertThat(yaml).doesNotContain("sk-");
    }
}
