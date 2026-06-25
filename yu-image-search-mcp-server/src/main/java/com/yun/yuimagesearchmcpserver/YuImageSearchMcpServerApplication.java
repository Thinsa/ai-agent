package com.yun.yuimagesearchmcpserver;

import com.yun.yuimagesearchmcpserver.tools.ImageSearchTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
/**
 * 图片搜索 MCP Server 启动入口。
 *
 * <p>独立启动后通过 MCP 协议暴露图片搜索工具，主应用可作为 MCP Client 动态调用。</p>
 */
public class YuImageSearchMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuImageSearchMcpServerApplication.class, args);
    }

    @Bean
    /**
     * 注册 MCP 工具回调提供者。
     *
     * <p>把 ImageSearchTool 的 @Tool 方法包装为 Spring AI ToolCallback，供 MCP Server 对外暴露。</p>
     */
    public ToolCallbackProvider imageSearchToolCallbackProvider(ImageSearchTool imageSearchTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(imageSearchTool)
                .build();
    }
}
