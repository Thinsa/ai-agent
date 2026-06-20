package com.yun.yunaiagent.agent;

import com.yun.yunaiagent.agent.model.AgentState;
import com.yun.yunaiagent.common.ValidationUtils;
import com.yun.yunaiagent.constants.Constants;
import com.yun.yunaiagent.chat.ChatHistoryService;
import com.yun.yunaiagent.tools.AgentTool;
import com.yun.yunaiagent.tools.TerminateTool;
import com.yun.yunaiagent.user.AppUser;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.List;
import java.util.stream.Collectors;

public class ToolCallAgent extends ReActAgent {

    private static final String MODULE = "super";

    private static final int MEMORY_WINDOW_SIZE = Constants.MEMORY_WINDOW_SIZE;

    protected final List<AgentTool> tools;

    private final ChatClient chatClient;

    private final ToolCallbackProvider toolCallbackProvider;

    private final ChatHistoryService chatHistoryService;

    private final String chatId;

    private final AppUser user;

    public ToolCallAgent(List<AgentTool> tools) {
        this(tools, null, null, null, null, null);
    }

    public ToolCallAgent(List<AgentTool> tools, ChatModel chatModel, ToolCallbackProvider toolCallbackProvider) {
        this(tools, chatModel, toolCallbackProvider, null, null, null);
    }

    public ToolCallAgent(List<AgentTool> tools, ChatModel chatModel, ToolCallbackProvider toolCallbackProvider,
                         ChatHistoryService chatHistoryService, String chatId, AppUser user) {
        this.tools = tools;
        this.chatClient = chatModel == null ? null : ChatClient.builder(chatModel).build();
        this.toolCallbackProvider = toolCallbackProvider;
        this.chatHistoryService = chatHistoryService;
        this.chatId = ValidationUtils.normalize(chatId, "default");
        this.user = user;
    }

    @Override
    protected void beforeRun(String userPrompt) {
        if (chatHistoryService != null) {
            chatHistoryService.appendUserMessage(MODULE, chatId, normalize(userPrompt), user);
        }
    }

    @Override
    protected void afterRun(String userPrompt, String result) {
        if (chatHistoryService != null && result != null && !result.isBlank()) {
            chatHistoryService.appendAssistantMessage(MODULE, chatId, result, user);
        }
    }

    @Override
    protected String toAssistantMessage(String result) {
        if (result == null || result.isBlank()) {
            return "";
        }
        return result.lines()
                .filter(line -> !line.matches("^Step\\s+\\d+:.*"))
                .collect(Collectors.joining(System.lineSeparator()))
                .trim();
    }

    @Override
    protected String think(String userPrompt, int stepNumber) {
        return "思考任务目标与可用工具。";
    }

    @Override
    protected String act(String userPrompt, int stepNumber) {
        if (chatClient != null && toolCallbackProvider != null) {
            String response = chatClient.prompt()
                    .system(systemPrompt())
                    .user(normalize(userPrompt))
                    .toolCallbacks(toolCallbackProvider)
                    .call()
                    .content();
            if (response != null && response.contains("任务已终止")) {
                state = AgentState.FINISHED;
            }
            if (stepNumber >= maxSteps) {
                state = AgentState.FINISHED;
            }
            return response == null || response.isBlank() ? "模型未返回工具执行结果。" : response;
        }
        state = AgentState.FINISHED;
        String toolNames = tools.stream()
                .map(AgentTool::name)
                .collect(Collectors.joining(", "));
        String terminateResult = tools.stream()
                .filter(TerminateTool.class::isInstance)
                .map(TerminateTool.class::cast)
                .findFirst()
                .map(TerminateTool::doTerminate)
                .orElse("未调用终止工具");
        return "当前智能体已注册工具：" + toolNames + "。任务：" + normalize(userPrompt) + System.lineSeparator() + terminateResult;
    }

    private String systemPrompt() {
        String memory = recentMemory();
        String prompt = """
                You are YuManus, a ReAct-style local agent. Use the registered tools when they help.
                Use searchWeb when the user needs current or internet information.
                Use generateImage when the user asks to generate, draw, create, or make an image.
                Default to generating 1 image. Only set count > 1 if the user explicitly asks for a specific number (e.g. "generate 2 images", "make three pictures"). Maximum 3 images per call.

                IMPORTANT RULES for image generation:
                - Call generateImage exactly ONCE per user request.
                - After calling generateImage, immediately call doTerminate — do NOT continue the loop.
                - Never call generateImage more than once. The count parameter controls how many images are generated in one call.
                - If generateImage fails, just report the error and call doTerminate. Do NOT retry.

                GENERAL RULES:
                - Keep the answer concise, include useful tool results such as image URLs.
                - Call doTerminate when the task is complete.
                - Never write Python scripts or shell commands to generate images.
                """;
        if (memory.isBlank()) {
            return prompt;
        }
        return prompt + System.lineSeparator() + """
                Recent conversation history:
                %s
                """.formatted(memory);
    }

    private String recentMemory() {
        if (chatHistoryService == null) {
            return "";
        }
        return chatHistoryService.recentMessages(MODULE, chatId, MEMORY_WINDOW_SIZE, user)
                .stream()
                .map(message -> message.getRole() + ": " + message.getContent())
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
