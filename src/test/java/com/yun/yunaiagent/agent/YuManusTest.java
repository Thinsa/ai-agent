package com.yun.yunaiagent.agent;

import com.yun.yunaiagent.tools.AgentTool;
import com.yun.yunaiagent.tools.TerminateTool;
import com.yun.yunaiagent.chat.ChatHistoryService;
import com.yun.yunaiagent.agent.model.AgentState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import org.mockito.ArgumentCaptor;

class YuManusTest {

    @Test
    void runListsToolsAndTerminatesWhenTerminateToolIsAvailable() {
        YuManus yuManus = YuManus.withTools(List.of(new TerminateTool()));

        String result = yuManus.run("完成后终止");

        assertThat(result).contains("doTerminate");
        assertThat(result).contains("任务已终止");
    }

    @Test
    void runIncludesAllRegisteredToolNames() {
        AgentTool customTool = new AgentTool() {
            @Override
            public String name() {
                return "customTool";
            }

            @Override
            public String description() {
                return "自定义工具";
            }
        };
        YuManus yuManus = YuManus.withTools(List.of(customTool));

        assertThat(yuManus.run("列出工具")).contains("customTool");
    }
    @Test
    void runStreamPersistsAssistantVisibleAnswerWithoutStepWrapper() {
        ChatHistoryService chatHistoryService = mock(ChatHistoryService.class);
        YuManus yuManus = new YuManus(List.of(new TerminateTool()), null, null, chatHistoryService, "super_test", null);

        yuManus.runStream("What is Java?");

        ArgumentCaptor<String> answerCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatHistoryService, timeout(1000).atLeastOnce()).appendAssistantMessage(
                eq("super"),
                eq("super_test"),
                answerCaptor.capture(),
                isNull()
        );

        assertThat(answerCaptor.getValue())
                .doesNotStartWith("Step ")
                .contains("What is Java?")
                .contains("\u4efb\u52a1\u5df2\u7ec8\u6b62");
    }

    @Test
    void uploadedImageStreamStopsAfterFirstVisionStep() {
        ChatHistoryService chatHistoryService = mock(ChatHistoryService.class);
        RepeatingVisionAgent agent = new RepeatingVisionAgent(chatHistoryService);
        agent.setImageUrl("https://oss.example/uploads/images/pokemo.png");

        agent.runStream("请描述这张图片");

        ArgumentCaptor<String> answerCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatHistoryService, timeout(1000).atLeastOnce()).appendAssistantMessage(
                eq("super"),
                eq("vision_test"),
                answerCaptor.capture(),
                isNull()
        );

        assertThat(agent.actCalls).isEqualTo(1);
        assertThat(answerCaptor.getValue())
                .contains("first vision description")
                .doesNotContain("second duplicate description");
    }

    static class RepeatingVisionAgent extends ToolCallAgent {

        private int actCalls;

        RepeatingVisionAgent(ChatHistoryService chatHistoryService) {
            super(List.of(new TerminateTool()), null, null, chatHistoryService, "vision_test", null);
        }

        @Override
        protected String think(String userPrompt, int stepNumber) {
            return "think";
        }

        @Override
        protected String act(String userPrompt, int stepNumber) {
            actCalls++;
            if (stepNumber == 1) {
                return "first vision description";
            }
            return "second duplicate description";
        }
    }
}
