package com.yun.yunaiagent.agent;

import com.yun.yunaiagent.chat.ChatHistoryService;
import com.yun.yunaiagent.tools.AgentTool;
import com.yun.yunaiagent.user.AppUser;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.List;

public class YuManus extends ToolCallAgent {

    public YuManus(List<AgentTool> tools) {
        super(tools);
        this.maxSteps = 20;
    }

    public YuManus(List<AgentTool> tools, ChatModel chatModel, ToolCallbackProvider toolCallbackProvider) {
        super(tools, chatModel, toolCallbackProvider);
        this.maxSteps = 20;
    }

    public YuManus(List<AgentTool> tools, ChatModel chatModel, ToolCallbackProvider toolCallbackProvider,
                   ChatHistoryService chatHistoryService, String chatId, AppUser user) {
        super(tools, chatModel, toolCallbackProvider, chatHistoryService, chatId, user);
        this.maxSteps = 20;
    }
}
