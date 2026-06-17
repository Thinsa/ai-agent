package com.yun.yunaiagent.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

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
}
