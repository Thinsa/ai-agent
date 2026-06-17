package com.yun.yunaiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
