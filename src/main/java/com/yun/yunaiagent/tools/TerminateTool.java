package com.yun.yunaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * 智能体终止工具。
 *
 * <p>用于让模型或调度器显式结束当前任务，避免继续无意义的推理循环。</p>
 */
@Component
public class TerminateTool implements AgentTool {

    @Override
    public String name() {
        return "doTerminate";
    }

    @Override
    public String description() {
        return "终止智能体任务";
    }

    /**
     * 返回终止信号的预留方法。
     */
    @Tool(description = "终止当前智能体任务")
    public String doTerminate() {
        return "任务已终止";
    }
}
