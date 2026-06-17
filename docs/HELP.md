# Yun AI Agent 帮助文档

## 项目定位

`yun-ai-agent` 是基于 Spring Boot 3.4、Spring AI 1.0、DashScope、PostgreSQL/PGVector 和 Vue 3 的 AI 应用项目。

核心能力：

- AI 恋爱大师：同步聊天、SSE 流式聊天、结构化报告、RAG、工具调用、MCP 工具入口。
- YuManus 超级智能体：ReAct 风格步骤输出，注册文件、网页、搜索、下载、终端、PDF、终止等工具。
- 用户系统：数据库持久化用户、注册后登录、JWT Bearer Token 鉴权、资料更新、无状态登出。
- 前端联调：`D:\html+css+js\ai-vue` 使用真实后端 API 和 localStorage token。

## 环境要求

- JDK 21
- Maven
- Node.js / npm
- PostgreSQL + pgvector
- DashScope API Key
- 可选：SearchAPI Key、MCP Server

## 本地配置

所有敏感配置都从环境变量读取。`application.yml` 不写默认账号密码或真实 key。

支持变量：

```text
DASHSCOPE_API_KEY
SEARCH_API_API_KEY
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
APP_JWT_SECRET
APP_JWT_EXPIRATION
APP_TOOLS_WORKSPACE
APP_TOOLS_COMMAND_TIMEOUT_SECONDS
APP_TOOLS_MAX_OUTPUT_CHARS
```

启动时会同步 `local-api-keys.txt`：

```text
当前进程环境变量 > Windows 用户环境变量 > txt 原值
```

控制台只打印变量名，不打印真实值。`local-api-keys.txt` 是本地私密文件，不应提交或分享。

从 txt 注入当前 PowerShell 进程：

```powershell
Get-Content .\local-api-keys.txt | ForEach-Object {
  if ($_ -match '^([^=]+)=(.*)$') {
    [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
  }
}
```

## 后端运行

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn test
mvn spring-boot:run
```

默认地址：

- `http://localhost:8123/api`
- `http://localhost:8123/api/health`
- `http://localhost:8123/api/swagger-ui.html`

## 前端运行

```powershell
cd D:\html+css+js\ai-vue
npm install
npm run dev
npm run build
```

开发环境请求 `http://localhost:8123/api`，生产环境使用相对路径 `/api`。

## 主要源码

| 主题 | 文件 |
|---|---|
| 启动类 | `src/main/java/com/yun/yunaiagent/YunAiAgentApplication.java` |
| AI 接口 | `src/main/java/com/yun/yunaiagent/controller/AiController.java` |
| 用户接口 | `src/main/java/com/yun/yunaiagent/controller/UserController.java` |
| 用户服务 | `src/main/java/com/yun/yunaiagent/user/` |
| JWT/Security | `src/main/java/com/yun/yunaiagent/security/`, `src/main/java/com/yun/yunaiagent/config/SecurityConfig.java` |
| LoveApp | `src/main/java/com/yun/yunaiagent/app/LoveApp.java` |
| RAG | `src/main/java/com/yun/yunaiagent/rag/` |
| 工具 | `src/main/java/com/yun/yunaiagent/tools/` |
| 智能体 | `src/main/java/com/yun/yunaiagent/agent/` |
| 前端 API | `D:\html+css+js\ai-vue\src\api\index.js` |
| 前端用户 store | `D:\html+css+js\ai-vue\src\stores\userStore.js` |

## RAG 与 PGVector

`spring.ai.vectorstore.pgvector.initialize-schema=true` 会让 Spring AI 初始化 PGVector schema。应用启动后 `LoveAppRagService` 会加载 `src/main/resources/document/*.md`，写入可用的 `VectorStore`。如果测试环境或本地环境没有 `VectorStore`，会回退到 classpath 文档上下文。

`/ai/love_app/chat/rag` 会先用 `QueryRewriter` 改写查询，再进行向量检索，并把检索上下文注入模型提示词。

## MCP

`application.yml` 保留了 MCP Client 配置示例。未配置 MCP Server 时，`/ai/love_app/chat/mcp` 返回清晰错误；配置了 MCP ToolCallbackProvider 后会把 MCP 工具交给 ChatClient。

## 工具安全边界

工具按开放能力实现，但保留最低保护：

- 终端命令超时：`APP_TOOLS_COMMAND_TIMEOUT_SECONDS`
- 输出截断：`APP_TOOLS_MAX_OUTPUT_CHARS`
- 工作目录限制：`APP_TOOLS_WORKSPACE`
- 异常返回文本，不让进程挂死
