package com.yun.yunaiagent.agent.model;

/**
 * Agent 运行状态枚举。
 *
 * <p>用于控制同步和 SSE 异步执行过程中的生命周期流转。</p>
 */
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
