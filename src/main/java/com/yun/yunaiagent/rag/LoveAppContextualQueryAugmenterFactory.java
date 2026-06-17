package com.yun.yunaiagent.rag;

import org.springframework.stereotype.Component;

/**
 * 上下文查询增强器工厂占位类。
 *
 * <p>用于在检索前结合会话上下文补全用户问题。</p>
 */
@Component
public class LoveAppContextualQueryAugmenterFactory {

    /**
     * 返回当前查询增强器配置说明，便于调试和接口展示。
     */
    public String createAugmenterDescription() {
        return "上下文查询增强工厂占位";
    }
}
