package com.yun.yunaiagent.agent;

import com.yun.yunaiagent.chat.ChatHistoryService;
import com.yun.yunaiagent.tools.AgentTool;
import com.yun.yunaiagent.user.AppUser;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.List;

/**
 * 项目内置的 Manus 风格智能体。
 *
 * <p>在 ToolCallAgent 基础上设置更长的最大执行步数，用于多工具协同任务。</p>
 */
public class YuManus extends ToolCallAgent {

    // Single primary constructor:
    public YuManus(List<AgentTool> tools, ChatModel chatModel,
                   ToolCallbackProvider toolCallbackProvider,
                   ChatHistoryService chatHistoryService, String chatId, AppUser user) {
        super(tools, chatModel, toolCallbackProvider, chatHistoryService, chatId, user);
        this.maxSteps = 20;
    }

    // Convenience factory:
    public static YuManus withTools(List<AgentTool> tools) {
        return new YuManus(tools, null, null, null, null, null);
    }

    // Convenience factory with model and tool provider:
    public static YuManus withToolsAndModel(List<AgentTool> tools, ChatModel chatModel,
            ToolCallbackProvider toolCallbackProvider) {
        return new YuManus(tools, chatModel, toolCallbackProvider, null, null, null);
    }
}
