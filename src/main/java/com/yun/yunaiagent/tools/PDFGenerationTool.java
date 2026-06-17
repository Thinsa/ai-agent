package com.yun.yunaiagent.tools;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * PDF 生成工具占位实现。
 *
 * <p>后续可接入模板引擎或 PDF 渲染库，把模型生成的报告落盘为文档。</p>
 */
@Component
public class PDFGenerationTool implements AgentTool {

    @Override
    public String name() {
        return "generatePDF";
    }

    @Override
    public String description() {
        return "PDF 生成工具骨架";
    }

    /**
     * 根据标题、正文和目标路径生成 PDF 的预留方法。
     */
    @Tool(description = "根据标题和正文生成 PDF 文件")
    public String generatePDF(String title, String content, String targetPath) {
        try {
            Path path = Path.of(targetPath);
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(path.toFile()));
            document.open();
            document.add(new Paragraph(title == null ? "Untitled" : title));
            document.add(new Paragraph(content == null ? "" : content));
            document.close();
            return "PDF 生成成功：" + path;
        } catch (Exception e) {
            return "PDF 生成失败：" + e.getMessage();
        }
    }
}
