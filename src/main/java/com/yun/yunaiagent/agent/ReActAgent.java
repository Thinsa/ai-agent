package com.yun.yunaiagent.agent;

public abstract class ReActAgent extends BaseAgent {

    /**
     * ReAct 基础流程：先思考（Reason），再行动（Act），最后合并本轮输出。
     */
    @Override
    protected String step(String userPrompt, int stepNumber) {
        String thought = think(userPrompt, stepNumber);
        String actionResult = act(userPrompt, stepNumber);
        return "Step " + stepNumber + ": " + thought + System.lineSeparator() + actionResult;
    }

    /**
     * 生成本轮推理内容，通常用于分析任务目标和下一步工具选择。
     */
    protected abstract String think(String userPrompt, int stepNumber);

    /**
     * 执行本轮动作，通常用于调用工具、观察结果或决定终止。
     */
    protected abstract String act(String userPrompt, int stepNumber);
}
