package com.yun.yunaiagent.agent;

import com.yun.yunaiagent.agent.model.AgentState;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class BaseAgent {

    /**
     * 智能体当前状态，volatile 确保 runStream() 异步线程的修改对调用方可见。
     */
    protected volatile AgentState state = AgentState.IDLE;

    /**
     * 最大执行步数，防止智能体陷入无限循环。
     */
    protected int maxSteps = 20;

    /**
     * 简单对话历史容器，后续可替换为更完整的 memory/session 机制。
     */
    protected final List<String> messageList = new ArrayList<>();

    /**
     * 当前正在执行的异步任务，用于在异常或完成时取消，避免线程泄漏。
     */
    private volatile CompletableFuture<Void> currentTask;

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
        String finalResult = toAssistantMessage(result.toString().trim());
        afterRun(userPrompt, finalResult);
        return finalResult;
    }

    /**
     * 以 SSE 方式异步执行任务，边执行边把每一步结果推送给客户端。
     */
    public SseEmitter runStream(String userPrompt) {
        SseEmitter emitter = createEmitter(300000L);
        CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
            StringBuilder result = new StringBuilder();
            boolean persisted = false;
            try {
                state = AgentState.RUNNING;
                messageList.add("User: " + normalize(userPrompt));
                beforeRun(userPrompt);
                for (int i = 1; i <= maxSteps && state == AgentState.RUNNING; i++) {
                    String stepResult = step(userPrompt, i);
                    result.append(stepResult).append(System.lineSeparator());
                    try {
                        emitter.send(toAssistantMessage(stepResult));
                    } catch (IOException sendError) {
                        if (isClientDisconnect(sendError)) {
                            state = AgentState.FINISHED;
                            break;
                        }
                        throw sendError;
                    }
                }
                if (state == AgentState.RUNNING) {
                    state = AgentState.FINISHED;
                }
                afterRun(userPrompt, toAssistantMessage(result.toString().trim()));
                persisted = true;
                try {
                    emitter.send("[DONE]");
                    emitter.complete();
                } catch (IOException ignored) {
                    emitter.complete();
                }
            } catch (IOException e) {
                state = isClientDisconnect(e) ? AgentState.FINISHED : AgentState.ERROR;
                if (!persisted && !result.toString().isBlank()) {
                    afterRun(userPrompt, toAssistantMessage(result.toString().trim()));
                }
                if (isClientDisconnect(e)) {
                    emitter.complete();
                } else {
                    emitter.completeWithError(e);
                }
            } finally {
                currentTask = null;
            }
        });
        currentTask = task;
        return emitter;
    }

    protected SseEmitter createEmitter(long timeoutMillis) {
        return new SseEmitter(timeoutMillis);
    }

    private boolean isClientDisconnect(IOException e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        return message.contains("你的主机中的软件中止了一个已建立的连接")
                || message.contains("An established connection was aborted")
                || message.contains("Broken pipe")
                || message.contains("Connection reset by peer");
    }

    /**
     * 单步执行钩子，由不同智能体定义自己的思考、行动和终止策略。
     */
    protected abstract String step(String userPrompt, int stepNumber);

    protected void beforeRun(String userPrompt) {
    }

    protected void afterRun(String userPrompt, String result) {
    }

    protected String toAssistantMessage(String result) {
        return result == null ? "" : result.trim();
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
