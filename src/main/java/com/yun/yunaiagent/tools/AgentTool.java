package com.yun.yunaiagent.tools;

/**
 * 智能体工具的统一抽象。
 *
 * <p>所有工具都暴露名称和描述，方便后续统一注册给模型或智能体调度器。</p>
 */
public interface AgentTool {

    /**
     * 工具唯一名称，通常会作为模型工具调用时的函数名。
     */
    String name();

    /**
     * 工具能力说明，用于帮助模型或调用方理解何时使用该工具。
     */
    String description();
}
