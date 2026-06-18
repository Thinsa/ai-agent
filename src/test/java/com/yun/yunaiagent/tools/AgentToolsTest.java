package com.yun.yunaiagent.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AgentToolsTest {

    @TempDir
    Path tempDir;

    @Test
    void fileToolReadsAndWritesRealFiles() throws Exception {
        FileOperationTool tool = new FileOperationTool();
        Path file = tempDir.resolve("note.txt");

        String writeResult = tool.writeFile(file.toString(), "hello");
        String readResult = tool.readFile(file.toString());

        assertThat(writeResult).contains("写入成功");
        assertThat(readResult).isEqualTo("hello");
    }

    @Test
    void terminalToolExecutesCommandWithTimeoutAndOutputLimit() {
        TerminalOperationTool tool = new TerminalOperationTool(5, 20);

        String result = tool.executeTerminalCommand("cmd /c echo hello-terminal");

        assertThat(result).contains("hello-terminal");
    }

    @Test
    void pdfToolCreatesPdfFile() throws Exception {
        PDFGenerationTool tool = new PDFGenerationTool();
        Path pdf = tempDir.resolve("report.pdf");

        String result = tool.generatePDF("标题", "正文内容", pdf.toString());

        assertThat(result).contains("PDF 生成成功");
        assertThat(Files.exists(pdf)).isTrue();
        assertThat(Files.size(pdf)).isGreaterThan(0);
    }

    @Test
    void webSearchToolReturnsClearMessageWhenApiKeyIsMissing() {
        WebSearchTool tool = new WebSearchTool("", (query, apiKey) -> "{}");

        String result = tool.searchWeb("Java 21");

        assertThat(result).contains("搜索失败：未配置");
    }

    @Test
    void webSearchToolFormatsAnswerBoxAndOrganicResults() {
        String json = """
                {
                  "answer_box": {"answer": "Java 21 是长期支持版本。"},
                  "organic_results": [
                    {"title": "Java 21 Guide", "link": "https://example.com/java21", "snippet": "Java 21 features"},
                    {"title": "Second", "link": "https://example.com/second", "snippet": "Second result"}
                  ]
                }
                """;
        WebSearchTool tool = new WebSearchTool("search-key", (query, apiKey) -> json);

        String result = tool.searchWeb("Java 21");

        assertThat(result)
                .contains("搜索摘要")
                .contains("Java 21 是长期支持版本。")
                .contains("1. Java 21 Guide")
                .contains("https://example.com/java21")
                .contains("Java 21 features");
    }

    @Test
    void imageGenerationToolReturnsClearMessageWhenDashScopeKeyIsMissing() {
        ImageGenerationTool tool = new ImageGenerationTool(
                new ImageGenerationProperties("", "qwen-image-2.0-pro", "2048*2048", ""),
                prompt -> "https://dashscope.example/temp.png",
                url -> new ImageGenerationTool.DownloadedImage(new byte[]{1}, "image/png"),
                (key, bytes, contentType) -> "https://oss.example/generated.png"
        );

        String result = tool.generateImage("cat poster");

        assertThat(result).contains("图片生成失败：未配置 DASHSCOPE_API_KEY");
    }

    @Test
    void imageGenerationToolUploadsGeneratedImageToOss() {
        ImageGenerationTool tool = new ImageGenerationTool(
                new ImageGenerationProperties("dashscope-key", "qwen-image-2.0-pro", "2048*2048", ""),
                prompt -> "https://dashscope.example/temp.png",
                url -> new ImageGenerationTool.DownloadedImage(new byte[]{1, 2, 3}, "image/png"),
                (key, bytes, contentType) -> {
                    assertThat(key).startsWith("generated-images/");
                    assertThat(bytes).containsExactly(1, 2, 3);
                    assertThat(contentType).isEqualTo("image/png");
                    return "https://oss.example/generated.png";
                }
        );

        String result = tool.generateImage("cat poster");

        assertThat(result)
                .contains("图片生成成功")
                .contains("https://oss.example/generated.png");
    }
}
