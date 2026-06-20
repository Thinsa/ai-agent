# Backend Architecture Cleanup — Design Spec

**Date:** 2026-06-20  
**Status:** approved  
**Scope:** Phase 1 — backend architecture cleanup (controller decomposition, SSE deduplication, shared utilities, magic-number constants)

## Goals

1. Split the God Controller `AiController` (339 lines, 13 endpoints, 4 domains) into focused controllers.
2. Extract the repeated SSE save→stream→save-on-complete pattern into a centralized `StreamingChatService`.
3. Extract duplicated `currentUser`/`currentUserId` resolution into `SecurityUtils`.
4. Consolidate scattered magic numbers into a `Constants` class.
5. Deduplicate string normalization across 5+ classes into `ValidationUtils`.
6. Remove redundant Spring MVC CORS config (keep only Spring Security CORS).
7. Replace constructor telescoping in `ToolCallAgent`, `YuManus`, and `LoveApp` with Builder pattern.

## Non-Goals

- Agent hierarchy changes (BaseAgent → ReActAgent → ToolCallAgent → YuManus stays as-is).
- Any endpoint URL or response format changes — front-end compatibility is preserved.
- Security hardening (deferred to Phase 2).
- Frontend refactoring (deferred to Phase 3).
- Test coverage improvements (deferred to Phase 2/3).

---

## 1. Controller Decomposition

### Before

```
AiController.java (339 lines)
  /ai/chat/sse              — basic chat
  /ai/upload                — file upload
  /ai/story/sse             — interactive story
  /ai/love_app/chat/sync    — LoveApp sync
  /ai/love_app/chat/sse     — LoveApp SSE
  /ai/love_app/chat/server_sent_event
  /ai/love_app/chat/sse_emitter
  /ai/love_app/chat/report
  /ai/love_app/chat/rag
  /ai/love_app/chat/tools
  /ai/love_app/chat/mcp
  /ai/manus/chat            — YuManus agent
  /ai/manus/chat/mcp        — YuManus agent + MCP
```

### After

```
ChatController.java         — /ai/chat/sse, /ai/upload
StoryController.java        — /ai/story/sse
LoveAppController.java      — /ai/love_app/chat/*  (9 endpoints, delegates to LoveApp)
ManusController.java        — /ai/manus/chat, /ai/manus/chat/mcp
```

`AiController.java` is deleted. All endpoint URLs are preserved exactly.

`STORY_SYSTEM_PROMPT` moves from `AiController` into a `private static final String` inside `StoryController`.

---

## 2. StreamingChatService — SSE Pattern Deduplication

### Problem

Four locations repeat the same save→stream→save-on-complete pattern:

| Location | Method |
|----------|--------|
| `AiController` | `doChatBasic()` |
| `AiController` | `doStoryChat()` |
| `LoveApp` | `doChatByStream()` |
| `LoveApp` | `doChatByStreamWithImage()` |

### Solution

```java
@Service
public class StreamingChatService {

    /**
     * Executes a complete save→stream→save-on-complete pipeline.
     *
     * @param module          chat module key ("chat", "love", etc.)
     * @param chatId          conversation id
     * @param userMessage     user's message text
     * @param systemPrompt    system prompt to inject
     * @param chatClient      pre-built Spring AI ChatClient (with tools/prompts already configured)
     * @param historyService  ChatHistoryService for persistence
     * @param onChunk         callback for each SSE chunk (used by SseEmitter/Flux senders)
     * @return                complete assistant response text
     */
    public String streamAndPersist(
            String module,
            String chatId,
            String userMessage,
            String systemPrompt,
            ChatClient chatClient,
            ChatHistoryService historyService,
            Consumer<String> onChunk
    );
}
```

- Callers still **build their own ChatClient** (with their own prompts, tool callbacks, RAG context).
- Callers still **configure their own SSE transport** (Flux, SseEmitter, etc.).
- `ToolCallAgent` is NOT touched — its save/run/save pattern is internal to the Agent hierarchy and differs from user-chat flow.

---

## 3. SecurityUtils — currentUser Deduplication

### Problem

Four controllers each implement their own `currentUser()`/`currentUserId()` variant.

### Solution

```java
public final class SecurityUtils {
    private SecurityUtils() {}

    /** Resolves user from Authentication first, falls back to token query param. */
    public static AppUser currentUser(
            Authentication authentication,
            String tokenParam,
            JwtService jwtService,
            UserService userService
    );

    /** Resolves user ID from Authentication only. */
    public static Long currentUserId(Authentication authentication, JwtService jwtService);
}
```

Callers:
- `ChatController`, `StoryController`, `LoveAppController`, `ManusController` → `SecurityUtils.currentUser(auth, token, jwtService, userService)`
- `ChatHistoryController`, `BackgroundController`, `StorySaveController` → `SecurityUtils.currentUserId(auth, jwtService)`

---

## 4. Constants — Magic Number Consolidation

Create `com.yun.yunaiagent.constants.Constants`:

```java
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
    public static final int COMMAND_TIMEOUT_SECONDS = 30;
    public static final int MAX_OUTPUT_CHARS = 12_000;
}
```

Every magic-number literal in the codebase is replaced with a reference.

---

## 5. ValidationUtils — String Normalization Deduplication

Add to `ValidationUtils`:

```java
public static String normalize(String value, String fallback) {
    return (value == null || value.isBlank()) ? fallback : value.trim();
}
```

Remove standalone `normalize()` methods from: `BaseAgent`, `LoveApp`, `AppUser`, `ToolCallAgent`.
Replace inline normalizations in `AiController` with `ValidationUtils.normalize(...)`.

---

## 6. CORS Deduplication

- Delete `CorsConfig.java` (Spring MVC `WebMvcConfigurer`-based CORS).
- Keep `SecurityConfig.java` (Spring Security `CorsConfigurationSource` bean).
- Both configure identical CORS (`*` origins, GET/POST/PUT/DELETE/OPTIONS, `*` headers, no credentials, 3600s max age). One is redundant.

---

## 7. Constructor Telescoping → Builder

### ToolCallAgent

Replace 3 constructors with:

```java
@Builder
public class ToolCallAgent extends ReActAgent {
    @Builder.Default private int maxSteps = 20;
    private ChatModel chatModel;
    private ToolCallbackProvider toolCallbackProvider;
    private ChatHistoryService chatHistoryService;
}
```

### YuManus

Replace 3 constructors with:

```java
@Builder
public class YuManus extends ToolCallAgent {
    @Builder.Default private int maxSteps = 20;
}
```

### LoveApp

Replace 2 constructors with:

```java
@Builder
public class LoveApp {
    private ChatModel chatModel;
    private ChatHistoryService chatHistoryService;
}
```

All existing constructor signatures remain callable via `Builder().build()`. Call sites are updated during implementation.

---

## Files Changed

| File | Action |
|------|--------|
| `AiController.java` | **Delete** |
| `ChatController.java` | **Create** (2 endpoints) |
| `StoryController.java` | **Create** (1 endpoint) |
| `LoveAppController.java` | **Create** (9 endpoints, delegation) |
| `ManusController.java` | **Create** (2 endpoints) |
| `StreamingChatService.java` | **Create** |
| `SecurityUtils.java` | **Create** |
| `Constants.java` | **Create** |
| `ValidationUtils.java` | **Modify** (add `normalize()` + `normalize(String, String)`) |
| `LoveApp.java` | **Modify** (use StreamingChatService, Builder, normalize) |
| `ToolCallAgent.java` | **Modify** (Builder, constants, normalize) |
| `YuManus.java` | **Modify** (Builder) |
| `BaseAgent.java` | **Modify** (remove normalize, use ValidationUtils, constants) |
| `AppUser.java` | **Modify** (remove normalize) |
| `ChatHistoryService.java` | **Modify** (use constants) |
| `ImageGenerationTool.java` | **Modify** (use constants) |
| `WebSearchTool.java` | **Modify** (use constants) |
| `WebScrapingTool.java` | **Modify** (use constants) |
| `TerminalOperationTool.java` | **Modify** (use constants) |
| `LoveAppRagService.java` | **Modify** (use constants) |
| `ChatHistoryController.java` | **Modify** (use SecurityUtils) |
| `BackgroundController.java` | **Modify** (use SecurityUtils) |
| `StorySaveController.java` | **Modify** (use SecurityUtils) |
| `CorsConfig.java` | **Delete** |
| `SecurityConfig.java` | **Modify** (unchanged, keep as sole CORS config) |

---

## Verification

1. `mvn test` — all existing 17 test classes pass. Test call sites that construct `ToolCallAgent`, `YuManus`, or `LoveApp` are updated to use the Builder pattern.
2. Manual endpoint smoke test — each of the 13 endpoints returns the same response shape as before.
3. Frontend smoke test — all 4 agent views (Chat, Love, Story, Manus) stream correctly.
