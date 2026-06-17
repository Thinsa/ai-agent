package com.yun.yunaiagent.tools;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
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
            FileOperationTool fileOperationTool,
            WebSearchTool webSearchTool,
            WebScrapingTool webScrapingTool,
            ResourceDownloadTool resourceDownloadTool,
            TerminalOperationTool terminalOperationTool,
            PDFGenerationTool pdfGenerationTool,
            TerminateTool terminateTool
    ) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(
                        fileOperationTool,
                        webSearchTool,
                        webScrapingTool,
                        resourceDownloadTool,
                        terminalOperationTool,
                        pdfGenerationTool,
                        terminateTool
                )
                .build();
    }
}
