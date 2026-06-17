package com.yun.yunaiagent.rag;

import org.springframework.stereotype.Component;

/**
 * RAG Advisor 工厂占位类。
 *
 * <p>后续可在这里组装 Spring AI Advisor，把检索结果注入模型上下文。</p>
 */
@Component
public class LoveAppRagCustomAdvisorFactory {

    /**
     * 返回当前 Advisor 配置说明，便于调试和接口展示。
     */
    public String createAdvisorDescription() {
        return "自定义 RAG Advisor 工厂占位";
    }
}
