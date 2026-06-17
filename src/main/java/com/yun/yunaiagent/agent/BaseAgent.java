package com.yun.yunaiagent.agent;

import com.yun.yunaiagent.agent.model.AgentState;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class BaseAgent {

    /**
     * 智能体当前状态，用于控制执行循环是否继续。
     */
    protected AgentState state = AgentState.IDLE;

    /**
     * 最大执行步数，防止智能体陷入无限循环。
     */
    protected int maxSteps = 20;

    /**
     * 简单的对话历史容器，后续可替换为更完整的 memory/session 机制。
     */
    protected final List<String> messageList = new ArrayList<>();

    /**
     * 同步执行智能体任务，每一轮由子类实现具体 step 逻辑。
     */
    public String run(String userPrompt) {
        state = AgentState.RUNNING;
        messageList.add("User: " + normalize(userPrompt));
        beforeRun(userPrompt);
        StringBuilder result = new StringBuilder();
        for (int i = 1; i <= maxSteps && state == AgentState.RUNNING; i++) {
            String stepResult = step(userPrompt, i);
            result.append(stepResult).append(System.lineSeparator());
        }
        if (state == AgentState.RUNNING) {
            state = AgentState.FINISHED;
        }
        String finalResult = result.toString().trim();
        afterRun(userPrompt, finalResult);
        return finalResult;
    }

    /**
     * 以 SSE 方式异步执行任务，边执行边把每一步结果推送给客户端。
     */
    public SseEmitter runStream(String userPrompt) {
        SseEmitter emitter = new SseEmitter(300000L);
        CompletableFuture.runAsync(() -> {
            try {
                state = AgentState.RUNNING;
                messageList.add("User: " + normalize(userPrompt));
                beforeRun(userPrompt);
                StringBuilder result = new StringBuilder();
                for (int i = 1; i <= maxSteps && state == AgentState.RUNNING; i++) {
                    String stepResult = step(userPrompt, i);
                    result.append(stepResult).append(System.lineSeparator());
                    emitter.send(stepResult);
                }
                if (state == AgentState.RUNNING) {
                    state = AgentState.FINISHED;
                }
                afterRun(userPrompt, result.toString().trim());
                emitter.complete();
            } catch (IOException e) {
                state = AgentState.ERROR;
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    /**
     * 单步执行钩子，由不同智能体定义自己的思考、行动和终止策略。
     */
    protected abstract String step(String userPrompt, int stepNumber);

    protected void beforeRun(String userPrompt) {
    }

    protected void afterRun(String userPrompt, String result) {
    }

    /**
     * 规范化用户输入，确保后续步骤总能拿到可读的任务描述。
     */
    protected String normalize(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "空任务";
        }
        return prompt.trim();
    }
}
