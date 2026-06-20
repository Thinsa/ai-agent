package com.yun.yunaiagent.tools;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工具注册配置预留类。
 *
 * <p>当前各工具通过 {@code @Component} 自动注入；后续如需统一转换为
 * Spring AI ToolCallback，可在这里集中声明 Bean。</p>
 */
@Configuration
public class ToolRegistration {

    @Bean
    public ToolCallbackProvider agentToolCallbackProvider(
            ObjectProvider<FileOperationTool> fileOperationTool,
            WebSearchTool webSearchTool,
            WebScrapingTool webScrapingTool,
            ObjectProvider<ResourceDownloadTool> resourceDownloadTool,
            ObjectProvider<TerminalOperationTool> terminalOperationTool,
            PDFGenerationTool pdfGenerationTool,
            ImageGenerationTool imageGenerationTool,
            TerminateTool terminateTool
    ) {
        var builder = MethodToolCallbackProvider.builder()
                .toolObjects(
                        webSearchTool,
                        webScrapingTool,
                        pdfGenerationTool,
                        imageGenerationTool,
                        terminateTool
                );
        fileOperationTool.ifAvailable(builder::toolObjects);
        resourceDownloadTool.ifAvailable(builder::toolObjects);
        terminalOperationTool.ifAvailable(builder::toolObjects);
        return builder.build();
    }
}
