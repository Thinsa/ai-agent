package com.yun.yunaiagent.agent;

import com.yun.yunaiagent.tools.AgentTool;
import com.yun.yunaiagent.tools.TerminateTool;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YuManusTest {

    @Test
    void runListsToolsAndTerminatesWhenTerminateToolIsAvailable() {
        YuManus yuManus = new YuManus(List.of(new TerminateTool()));

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
        YuManus yuManus = new YuManus(List.of(customTool));

        assertThat(yuManus.run("列出工具")).contains("customTool");
    }
}
