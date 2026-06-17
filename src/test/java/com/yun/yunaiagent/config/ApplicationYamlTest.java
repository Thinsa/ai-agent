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
        assertThat(yaml).doesNotContain("sk-");
    }
}
