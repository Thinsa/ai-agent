package com.yun.yunaiagent.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LocalEnvironmentFileServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void mergeKeepsTxtValuesAndOverridesWithEnvironmentValuesWithoutPrintingSecrets() throws Exception {
        Path envFile = tempDir.resolve("local-api-keys.txt");
        Files.writeString(envFile, """
                DASHSCOPE_API_KEY=txt-dashscope
                SEARCH_API_API_KEY=txt-search
                APP_JWT_SECRET=txt-secret
                """);

        LocalEnvironmentFileService service = new LocalEnvironmentFileService();

        LocalEnvironmentFileService.MergeResult result = service.merge(
                envFile,
                Map.of(
                        "APP_JWT_SECRET", "user-secret",
                        "SPRING_DATASOURCE_URL", "jdbc:postgresql://localhost:5432/yun_ai_agent",
                        "PEXELS_API_KEY", "user-pexels",
                        "OPENAI_BASE_URL", "https://api.xiaomimimo.com/v1"
                ),
                Map.of(
                        "APP_JWT_SECRET", "process-secret",
                        "APP_TOOLS_MAX_OUTPUT_CHARS", "4000",
                        "PEXELS_API_KEY", "process-pexels",
                        "APP_MCP_CLIENT_ENABLED", "true",
                        "OPENAI_CHAT_MODEL", "mimo-v2.5"
                )
        );

        String merged = Files.readString(envFile);
        assertThat(merged).contains("DASHSCOPE_API_KEY=txt-dashscope");
        assertThat(merged).contains("SEARCH_API_API_KEY=txt-search");
        assertThat(merged).contains("APP_JWT_SECRET=process-secret");
        assertThat(merged).contains("SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/yun_ai_agent");
        assertThat(merged).contains("APP_TOOLS_MAX_OUTPUT_CHARS=4000");
        assertThat(merged).contains("PEXELS_API_KEY=process-pexels");
        assertThat(merged).contains("APP_MCP_CLIENT_ENABLED=true");
        assertThat(merged).contains("OPENAI_BASE_URL=https://api.xiaomimimo.com/v1");
        assertThat(merged).contains("OPENAI_CHAT_MODEL=mimo-v2.5");
        assertThat(result.writtenKeys()).contains("APP_JWT_SECRET", "SPRING_DATASOURCE_URL", "APP_TOOLS_MAX_OUTPUT_CHARS", "PEXELS_API_KEY");
        assertThat(result.safeSummary()).contains("APP_JWT_SECRET=set");
        assertThat(result.safeSummary()).doesNotContain("process-secret");
        assertThat(result.safeSummary()).doesNotContain("process-pexels");
    }

    @Test
    void mergeNormalizesBomAndUnexpectedPrefixesInExistingKeys() throws Exception {
        Path envFile = tempDir.resolve("local-api-keys-bom.txt");
        Files.writeString(envFile, """
                \uFEFFAPP_JWT_SECRET=txt-secret
                broken-prefix-PP_JWT_EXPIRATION=86400000
                """);

        LocalEnvironmentFileService service = new LocalEnvironmentFileService();

        LocalEnvironmentFileService.MergeResult result = service.merge(envFile, Map.of(), Map.of());

        String merged = Files.readString(envFile);
        assertThat(merged).contains("APP_JWT_SECRET=txt-secret");
        assertThat(merged).contains("APP_JWT_EXPIRATION=86400000");
        assertThat(merged).doesNotContain("broken-prefix-PP_JWT_EXPIRATION");
        assertThat(merged).doesNotContain("\uFEFFAPP_JWT_SECRET");
        assertThat(result.writtenKeys()).contains("APP_JWT_SECRET", "APP_JWT_EXPIRATION");
    }
}
