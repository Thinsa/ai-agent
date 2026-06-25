package com.yun.yunaiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 后端主应用入口。
 *
 * <p>负责启动 Spring Boot 容器，并让控制器、服务、RAG、Agent、工具等组件被自动扫描注册。</p>
 */
@SpringBootApplication
public class YunAiAgentApplication {

    /**
     * 项目启动入口。
     *
     * <p>数据库、PGVector、AI 模型和工具参数均通过 Spring 配置加载。</p>
     */
    public static void main(String[] args) {
        SpringApplication.run(YunAiAgentApplication.class, args);
    }
}
