package com.yun.yunaiagent.agent.model;

public enum AgentState {
    /** 等待任务输入。 */
    IDLE,
    /** 正在执行任务。 */
    RUNNING,
    /** 任务正常结束。 */
    FINISHED,
    /** 执行过程中出现异常。 */
    ERROR
}
