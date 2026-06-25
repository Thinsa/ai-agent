package com.yun.yunaiagent.constants;

/**
 * 全局常量集中定义。
 *
 * <p>用于统一维护对话记忆窗口、SSE 超时时间等跨模块共享配置，避免魔法值散落在业务代码中。</p>
 */
public final class Constants {
    private Constants() {}

    // Messages / conversations
    public static final int CONVERSATION_TITLE_MAX_LENGTH = 40;
    public static final int FILE_PREVIEW_MAX_CHARS = 2000;
    public static final int STORY_HISTORY_WINDOW = 30;
    public static final int MEMORY_WINDOW_SIZE = 20;

    // SSE timeouts (ms)
    public static final long SSE_EMITTER_TIMEOUT_MS = 180_000L;
    public static final long AGENT_SSE_TIMEOUT_MS = 300_000L;
    public static final long MCP_ERROR_TIMEOUT_MS = 30_000L;

    // RAG
    public static final int RAG_TOP_K = 4;
    public static final double RAG_SIMILARITY_THRESHOLD = 0.5;

    // Image generation
    public static final int MAX_IMAGES = 3;
    public static final int HTTP_CONNECT_TIMEOUT_SEC = 10;
    public static final int HTTP_READ_TIMEOUT_SEC = 120;

    // Web search
    public static final int WEB_SEARCH_MAX_RESULTS = 5;

    // Web scraping
    public static final int WEB_SCRAPE_TIMEOUT_MS = 20_000;

    // Terminal
    public static final int COMMAND_TIMEOUT_SECONDS_DEFAULT = 30;
    public static final int MAX_OUTPUT_CHARS_DEFAULT = 12_000;
}
