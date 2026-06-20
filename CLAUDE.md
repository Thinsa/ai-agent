# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Yun AI Agent — Spring Boot 3.4.4 + Spring AI 1.0.0 multi-LLM backend with a Vue 3 + Vite frontend. The platform provides an "AI Love Master" chatbot, a tool-calling ReAct-style agent (YuManus), RAG with PGVector, and MCP client integration. All sensitive configuration is environment-variable-driven with a `local-api-keys.txt` merge layer.

> **Note:** `AGENTS.md` contains a condensed version of this file for GitHub Copilot / coding agents. When updating architecture docs, keep both files in sync.

## Build & Run

**Requirements:** JDK 21, Maven, PostgreSQL with pgvector extension, DashScope API key.

```powershell
# Backend
$env:JAVA_HOME='C:\Program Files\Java\jdk-21'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn test                                    # Run all tests
mvn test -Dtest=AgentToolsTest             # Run a single test class
mvn test -Dtest=AgentToolsTest#fileToolReadsAndWritesRealFiles  # Run a single test method
mvn spring-boot:run                         # Start backend on port 8123

# Frontend (ai-vue/)
npm install
npm run dev                                 # Dev server
npm run build                               # Production build

# MCP image search server (separate project under yu-image-search-mcp-server/)
cd yu-image-search-mcp-server
$env:PEXELS_API_KEY='your-key'
mvn spring-boot:run                         # Runs on port 8127
```

**Default URLs:**
- Backend API: `http://localhost:8123/api`
- Health check: `http://localhost:8123/api/health`
- Swagger/Knife4j: `http://localhost:8123/api/swagger-ui.html`

## Architecture

### Agent hierarchy (`agent/`)

```
BaseAgent (abstract)
  └─ ReActAgent (abstract) — think() → act() loop
       └─ ToolCallAgent — uses Spring AI ChatClient + ToolCallbackProvider
            └─ YuManus — concrete agent, maxSteps=20
```

- `BaseAgent` provides the execution loop (sync `run()` and async `runStream()` SSE), state machine (IDLE→RUNNING→FINISHED/ERROR), and step count gating.
- `ReActAgent` defines the Reason-then-Act pattern: `step()` calls `think()` then `act()`.
- `ToolCallAgent` is where the real work happens: it delegates to a Spring AI `ChatClient` with `ToolCallbackProvider`, injects conversation history as system prompt memory (last 20 messages), and persists via `ChatHistoryService`. The module key for history is `"super"`.
- `YuManus` is a thin subclass that sets `maxSteps = 20`.
- `ToolCallAgent`'s system prompt enforces critical behavioral rules: call `generateImage` exactly once per request then immediately call `doTerminate`; never retry on image generation failure; never write scripts to generate images; default to 1 image, max 3.

### Tools (`tools/`)

All tools implement the `AgentTool` interface (`name()` + `description()`) and expose callable methods annotated with `@Tool`. They are registered as `ToolCallbackProvider` bean in `ToolRegistration`.

| Tool | Purpose | Key dependency |
|------|---------|---------------|
| `FileOperationTool` | Read/write text files | `java.nio.file` |
| `WebSearchTool` | Search via SearchAPI.io (Baidu engine) | `RestClient` |
| `WebScrapingTool` | Scrape webpage text | Jsoup |
| `ResourceDownloadTool` | Download URL to local file | `java.net.URI` |
| `TerminalOperationTool` | Execute shell commands with timeout/output limits | `ProcessBuilder` |
| `PDFGenerationTool` | Generate PDF files | OpenPDF |
| `ImageGenerationTool` | Generate images via DashScope API, upload to OSS | DashScope + Aliyun OSS |
| `TerminateTool` | Signal agent task completion | — |

`ImageGenerationTool` has a multi-step pipeline: generate temp image URL from DashScope → download image bytes → upload to Aliyun OSS → return public URL. It uses `@ConfigurationProperties(prefix = "dashscope.image")` via `ImageGenerationProperties`.

### Applications (`app/`)

`LoveApp` — the "AI Love Master" chatbot component (`@Component`). Module key for history is `"love"`. Provides:
- Basic chat (sync + SSE streaming via Reactor Flux), injecting last 20 messages as system-prompt memory
- Image multimodal chat (`doChatByStreamWithImage`) — accepts an image URL, passes it via Spring AI `Media` type to the model
- RAG-augmented chat (retrieves from PGVector, injects as system context with "请优先参考以下知识库内容回答" instruction)
- Tool-calling chat (with `ToolCallbackProvider`)
- MCP-enabled chat
- Structured report generation (`LoveReport` record with title, summary, suggestions)

### Controllers

- `AiController` (`/ai`) — all AI endpoints. Creates `YuManus` instances per-request. Supports both token-based auth (query param) and `Authentication`-based auth. Key endpoints:
  - `/chat/sse` — basic chat (Flux SSE), uses `ChatClient` directly with a "灵语 Spark" system prompt, module key `"chat"`.
  - `/story/sse` — interactive branching story chat (Flux SSE). Uses a detailed `STORY_SYSTEM_PROMPT` with rules for 分支式互动故事 (branching interactive stories): 2-4 plot options + "我自己决定" per turn, 3+ endings (good/bad/hidden), options formatted with `【选项】` markers. Loads last 30 messages as conversation history. The frontend `storyGuideCore.js` implements BFS pathfinding over story scenes to guide users toward specific endings.
  - `/upload` — file upload. Images go to OSS (`uploads/images/`) and return a public URL; text/code files go to local workspace with a preview (first 2000 chars).
  - `/love_app/chat/*` — LoveApp variants (sync, SSE Flux, ServerSentEvent, SseEmitter, report, RAG, tools, MCP).
  - `/manus/chat` and `/manus/chat/mcp` — YuManus agent via SseEmitter.
- `UserController` (`/user`) — register, login (returns JWT), profile update, avatar upload, token validation, logout.
- `ChatHistoryController` (`/ai/history`) — list sessions by module, get conversation detail.
- `HealthController` (`/health`) — public health endpoint.
- `BackgroundController` (`/backgrounds`) — per-user background image CRUD (OSS-hosted images + opacity).
- `StorySaveController` (`/story-saves`) — save, list, delete interactive story sessions.

### SSE Patterns

Three SSE approaches coexist in the codebase — use the right one for the context:

| Pattern | When used | Example |
|---------|-----------|---------|
| `Flux<String>` (Reactor) | Direct `ChatClient.stream().content()` — used by LoveApp and story chat | `AiController.doChatWithLoveAppSSE()` |
| `SseEmitter` (Spring MVC) | Agent execution loop (`BaseAgent.runStream()`) where steps are produced in a `CompletableFuture.runAsync()` thread | `AiController.doChatWithManus()` |
| `ServerSentEvent<String>` (Reactor wrapper) | When you need SSE metadata (event, id) alongside data chunks | `AiController.doChatWithLoveAppServerSentEvent()` |

All SSE endpoints produce `MediaType.TEXT_EVENT_STREAM_VALUE`. The `SseEmitter`-based agent loop sends `"[DONE]"` on completion (matching OpenAI SSE convention) and detects client disconnect via Chinese/English error messages in `IOException`.

### Security (`security/`)

Stateless JWT authentication with Spring Security:
- `JwtAuthenticationFilter` extracts Bearer token from `Authorization` header, sets `SecurityContextHolder`.
- `JwtService` uses HMAC-SHA (jjwt library), secret from `APP_JWT_SECRET` env var (falls back to a dev default if < 32 chars).
- Public endpoints: `/user/register`, `/user/login`, `/user/token/validate`, `/health`, `/swagger-ui/**`, `/v3/api-docs/**`. All `/user/current`, `/user/profile`, `/user/logout` require auth. AI endpoints are `permitAll()` but resolve user from token parameter.
- CORS is fully open (`allowedOriginPatterns("*")`).

### RAG (`rag/`)

- `LoveAppDocumentLoader` — loads documents from `classpath*:document/*.md`.
- `QueryRewriter` — rewrites user queries for better retrieval.
- `LoveAppRagService` (implements `ApplicationRunner`) — on startup, loads documents and indexes them into PGVector. On query, rewrites query → similarity search (topK=4, threshold=0.5) → returns context.
- PGVector config: HNSW index, COSINE_DISTANCE, 1536 dimensions (matching DashScope embeddings).
- `LoveAppVectorStoreConfig` and `LoveAppRagCustomAdvisorFactory` are placeholder classes.

### Chat History (`chat/`)

Two JPA entities: `ChatConversation` (module + chatId unique per conversation, with title) and `ChatMessage` (role + content, ordered by createdAt). `ChatHistoryService` auto-creates conversations on first message, binds the user on first authenticated message, and auto-generates a title from the first 40 chars of content.

**Module keys** — each chat domain uses a distinct module key to isolate conversations:
- `"love"` — LoveApp (恋爱大师) conversations
- `"super"` — YuManus agent conversations
- `"chat"` — basic chat and interactive story conversations

Both `LoveApp` and `ToolCallAgent` inject the last 20 messages (`MEMORY_WINDOW_SIZE = 20`) from their module as system-prompt memory via `recentMessages()`, formatted as `"role: content"` per line.

### Background Settings (`background/`)

`UserBackground` JPA entity stores per-user, per-agent-key background images (uploaded to OSS) with opacity. Used by the frontend `bgStore.js` to customize page backgrounds per agent view.

### Story Save (`storysave/`)

`StorySave` JPA entity persists user-saved interactive stories with CRUD via `StorySaveController` (`/story-saves`).

### Common (`common/`)

`ValidationUtils` — static helper for `required(value, field)` (throws 400 on blank) and `isBlank(value)`.

### Users (`user/`)

`AppUser` JPA entity with BCrypt password hashing. Avatar upload uses `AliyunOssAvatarStorageService` (implements `AvatarStorageService` interface).

### Configuration (`config/`)

- `LocalEnvironmentFileService` — merges environment variables into `local-api-keys.txt` with priority: process env > Windows user env > file values. Managed keys list covers DashScope, OpenAI, DB, JWT, tools, MCP, OSS, and Pexels keys.
- `LocalEnvironmentFileInitializer` (ApplicationRunner) — calls the merge on startup. Reads Windows user environment via `reg query HKCU\Environment`.
- `application.yml` — all sensitive values reference env vars (`${VAR_NAME:}`). Chat model is `openai` (OpenAI-compatible), embedding model is `dashscope`. Supports Ollama as an alternative model backend.

### LLM Backend Configuration

The project uses OpenAI-compatible protocol as the primary chat backend. The `openAiChatModel` qualifier bean is injected into `AiController` and `LoveApp`. DashScope is used for embeddings and image generation. Ollama is configured but not the active chat model.

## Frontend (`ai-vue/`)

Vue 3 (Options API / Composition API mix) with Vue Router and Axios:
- **Router** (`router/index.js`): Lazy-loaded routes — Login (guest-only), Home, UserProfile, LoveMaster, SuperAgent (all require auth). Navigation guard auto-restores session from localStorage token and redirects accordingly.
- **Store** (`stores/userStore.js`): Reactive auth state with localStorage persistence. `restoreSession()` validates token on app init. `login()` stores token + user. `logout()` clears both.
- **API** (`api/index.js`): Axios instance with base URL (`/api` in prod, `http://localhost:8123/api` in dev). Request interceptor attaches Bearer token; response interceptor clears auth on 401. SSE connections use `EventSource` with token as query param (since EventSource doesn't support custom headers).
- **Key components**: `ChatRoom.vue` (main chat UI), `ChatHistorySidebar.vue` (session list), `AgentSettingsPanel.vue` (agent configuration), `AiAvatarFallback.vue` (default avatar with initials), `StoryGuidePanel.vue` (ending guide overlay for interactive stories).
- **Additional stores**: `bgStore.js` (per-agent background images from `/backgrounds` API), `storyGuide.js` + `storyGuideCore.js` (interactive story ending pathfinding — BFS over story script scene graph to find shortest path from current scene to target ending), `fixedStoryStore.js` (pre-built story script data).

## MCP Server (`yu-image-search-mcp-server/`)

Independent Spring Boot project providing Pexels image search as an MCP tool. The main backend connects to it via Spring AI MCP client when `APP_MCP_CLIENT_ENABLED=true`.

## Testing

Tests use JUnit 5 + AssertJ. Key patterns:
- `ApplicationContextRunner` for Spring context integration tests (e.g., verifying `@ConfigurationProperties` or `@Value` injection).
- `@TempDir` for file-based tool tests.
- Tools are tested with constructor-injected functional interface mocks (e.g., `WebSearchTool(String apiKey, SearchApiClient searchApiClient)`).
- H2 in-memory database for test scope (JPA repository tests).
- Test naming convention: descriptive camelCase methods in English.

## Environment Variables Reference

Key variables (managed by `LocalEnvironmentFileService`):

| Variable | Purpose |
|----------|---------|
| `DASHSCOPE_API_KEY` | DashScope LLM/embedding/image API key |
| `OPENAI_API_KEY` / `OPENAI_BASE_URL` / `OPENAI_CHAT_MODEL` | OpenAI-compatible chat backend |
| `SEARCH_API_API_KEY` | SearchAPI.io key for web search |
| `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` | PostgreSQL connection |
| `APP_JWT_SECRET` / `APP_JWT_EXPIRATION` | JWT signing key and TTL (ms) |
| `APP_TOOLS_WORKSPACE/COMMAND_TIMEOUT_SECONDS/MAX_OUTPUT_CHARS` | Tool execution constraints |
| `APP_MCP_CLIENT_ENABLED` / `APP_MCP_IMAGE_SEARCH_URL` | MCP client toggle and server URL |
| `ALIYUN_OSS_*` | Aliyun OSS for image/avatar storage |
| `PEXELS_API_KEY` | Pexels API key (for MCP server) |
