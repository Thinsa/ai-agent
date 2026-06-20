# LinkMind 灵桥 / Yun AI Agent

<p align="center">
  <strong>基于 Spring Boot、Spring AI 与 Vue 3 的多智能体 AI 应用平台</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.4.4-6DB33F?logo=springboot" alt="Spring Boot 3.4.4" />
  <img src="https://img.shields.io/badge/Spring%20AI-1.0.0-6DB33F" alt="Spring AI 1.0.0" />
  <img src="https://img.shields.io/badge/Vue-3-4FC08D?logo=vue.js" alt="Vue 3" />
  <img src="https://img.shields.io/badge/Vite-4-646CFF?logo=vite" alt="Vite 4" />
  <img src="https://img.shields.io/badge/PostgreSQL-pgvector-4169E1?logo=postgresql" alt="PostgreSQL pgvector" />
</p>

---

## 简介

LinkMind 灵桥是一个 AI Agent 全栈项目，后端基于 Spring Boot 3.4、Spring AI 和 Spring AI Alibaba DashScope，前端基于 Vue 3 + Vite。项目围绕“陪伴、创作、工具执行”三个方向构建了多智能体体验，并集成用户系统、聊天历史、RAG 知识库、MCP 图片搜索、工具调用、故事保存、背景设置和头像上传等能力。

前端位于 `ai-vue/`，后端位于项目根目录 `src/`，独立图片搜索 MCP Server 位于 `yu-image-search-mcp-server/`。

## 功能特性

- **知心 Soul**：面向情感陪伴与恋爱咨询，支持 SSE 流式聊天、RAG 检索增强、报告生成和工具调用。
- **灵语 Spark**：互动剧本智能体，支持分支剧情推进、固定剧本、故事引导、进度保存和多结局体验。
- **极智 Core**：通用任务智能体，支持 Manus Agent、工具调度、联网搜索、图片生成、文件处理和 MCP 扩展。
- **RAG 知识库**：支持 classpath 文档与数据库自定义文档合并索引，基于 PostgreSQL + pgvector 检索，并支持重建索引。
- **Agent 工具箱**：包含文件操作、终端执行、网页抓取、Web 搜索、资源下载、PDF 生成、图片生成和终止工具。
- **MCP 扩展**：主后端可作为 MCP Client 连接独立图片搜索 MCP Server。
- **用户体系**：支持注册、登录、JWT 鉴权、当前用户、Token 校验、资料更新、头像上传和退出登录。
- **个性化配置**：支持聊天背景图、背景透明度、用户头像、聊天历史和故事收藏。

## 快速开始

### 环境要求

- JDK 21
- Maven 3.9+
- Node.js 18+ 和 npm
- PostgreSQL，并安装 pgvector 扩展
- DashScope API Key
- 可选：Pexels API Key，用于图片搜索 MCP Server
- 可选：阿里云 OSS 配置，用于头像或背景对象存储

### 进入项目

```powershell
cd D:\JavaMaterial\ai-agent
```

推荐先准备本地配置文件或环境变量，再启动后端。真实密钥不要提交到仓库。

## 本地配置

敏感配置可以放在环境变量、`src/main/resources/application-local.yml` 或 `local-api-keys.txt` 中。项目启动时会读取本地环境变量，并可合并 `local-api-keys.txt` 中的值。

常用配置项：

```text
DASHSCOPE_API_KEY
OPENAI_API_KEY
OPENAI_BASE_URL
OPENAI_CHAT_MODEL
SEARCH_API_API_KEY

SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD

APP_JWT_SECRET
APP_JWT_EXPIRATION
APP_TOOLS_WORKSPACE
APP_TOOLS_COMMAND_TIMEOUT_SECONDS
APP_TOOLS_MAX_OUTPUT_CHARS

APP_MCP_CLIENT_ENABLED
APP_MCP_IMAGE_SEARCH_URL
PEXELS_API_KEY

DASHSCOPE_IMAGE_MODEL
DASHSCOPE_IMAGE_SIZE
DASHSCOPE_IMAGE_NEGATIVE_PROMPT
DASHSCOPE_IMAGE_API_URL
DASHSCOPE_IMAGE_WATERMARK
DASHSCOPE_IMAGE_PROMPT_EXTEND

ALIYUN_OSS_ENDPOINT
ALIYUN_OSS_BUCKET
ALIYUN_OSS_ACCESS_KEY_ID
ALIYUN_OSS_ACCESS_KEY_SECRET
ALIYUN_OSS_PUBLIC_BASE_URL
ALIYUN_OSS_AVATAR_DIR
ALIYUN_OSS_MAX_AVATAR_SIZE_BYTES
```

PowerShell 临时加载 `local-api-keys.txt` 示例：

```powershell
Get-Content .\local-api-keys.txt | ForEach-Object {
  if ($_ -match '^([^=]+)=(.*)$') {
    [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
  }
}
```

## 启动后端

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21'
$env:Path="$env:JAVA_HOME\bin;$env:Path"

mvn test
mvn spring-boot:run
```

默认地址：

- Backend: `http://localhost:8123/api`
- Health: `http://localhost:8123/api/health`
- Swagger / Knife4j: `http://localhost:8123/api/swagger-ui.html`

## 启动前端

```powershell
cd .\ai-vue
npm install
npm run dev
```

默认地址：

- Dev server: `http://localhost:3000`
- API proxy: `/api` -> `http://localhost:8123`

生产构建：

```powershell
cd .\ai-vue
npm run build
```

如果 Windows PowerShell 因执行策略拦截 `npm.ps1`，可以改用：

```powershell
npm.cmd run build
```

## 启动 MCP 图片搜索服务

图片搜索 MCP Server 是独立 Maven 项目，位于 `yu-image-search-mcp-server/`，默认端口为 `8127`。

```powershell
cd .\yu-image-search-mcp-server
$env:PEXELS_API_KEY='your-pexels-key'
mvn spring-boot:run
```

启用主后端 MCP Client：

```powershell
$env:APP_MCP_CLIENT_ENABLED='true'
$env:APP_MCP_IMAGE_SEARCH_URL='http://localhost:8127'
mvn spring-boot:run
```

## 技术架构

| 层级 | 技术 | 说明 |
| --- | --- | --- |
| 后端框架 | Spring Boot 3.4.4 | REST、SSE、配置管理与应用启动 |
| AI 编排 | Spring AI 1.0.0 / Spring AI Alibaba | Chat Model、Embedding、VectorStore、MCP Client |
| 模型接入 | DashScope / OpenAI compatible / Ollama | 聊天、Embedding、图片生成等模型能力 |
| 鉴权 | Spring Security + JWT | 登录态、用户身份和接口保护 |
| 持久化 | Spring Data JPA | 用户、聊天、故事、背景和知识库文档 |
| 向量检索 | PostgreSQL + pgvector | RAG 文档向量化与相似度检索 |
| API 文档 | Knife4j / springdoc-openapi | Swagger UI 与接口调试 |
| 前端 | Vue 3 / Vite 4 / Vue Router / Axios | 单页应用、路由、请求封装和页面状态 |
| MCP 服务 | Spring Boot 独立模块 | 图片搜索工具服务，供主后端 MCP Client 调用 |

## 项目结构

```text
.
|-- ai-vue/                         # Vue 3 + Vite 前端
|   |-- src/
|   |   |-- api/                    # 前端 API 封装
|   |   |-- assets/                 # 固定剧本等静态资源
|   |   |-- components/             # 通用组件与聊天组件
|   |   |-- router/                 # Vue Router
|   |   |-- stores/                 # 前端状态与业务逻辑
|   |   `-- views/                  # 页面视图
|   `-- package.json
|-- src/
|   |-- main/
|   |   |-- java/com/yun/yunaiagent/
|   |   |   |-- agent/              # Agent 基类、ReAct、工具调用和 YuManus
|   |   |   |-- app/                # 业务 AI 应用
|   |   |   |-- background/         # 用户背景图配置
|   |   |   |-- chat/               # 聊天会话与消息历史
|   |   |   |-- common/             # 通用工具
|   |   |   |-- config/             # CORS、安全、本地配置加载
|   |   |   |-- controller/         # REST / SSE 接口
|   |   |   |-- rag/                # RAG 文档加载、检索、查询改写
|   |   |   |-- security/           # JWT 鉴权
|   |   |   |-- storysave/          # 故事保存
|   |   |   |-- tools/              # Agent 可调用工具
|   |   |   `-- user/               # 用户、头像和 OSS 配置
|   |   `-- resources/
|   |       |-- application.yml
|   |       |-- application-local.yml
|   |       `-- document/           # RAG 文档资源
|   `-- test/java/                  # 后端测试
|-- yu-image-search-mcp-server/     # 独立图片搜索 MCP Server
|-- pom.xml                         # 后端 Maven 项目
`-- README.md
```

## 常用接口

后端统一使用 `/api` 作为上下文路径。

- `GET /api/health`：健康检查
- `/api/user/*`：注册、登录、当前用户、Token 校验、头像上传和退出登录
- `/api/ai/chat/sse`：通用聊天 SSE
- `/api/ai/love_app/*`：知心 Soul 聊天、RAG、工具调用和 MCP 调用
- `/api/ai/manus/*`：极智 Core / Manus Agent 流式聊天
- `/api/ai/story/sse`：灵语 Spark 故事生成接口
- `/api/ai/history/*`：聊天会话与消息历史
- `/api/knowledge-documents/*`：知识库文档管理与重建索引
- `/api/story-saves/*`：故事保存、列表和删除
- `/api/backgrounds/*`：用户背景图配置

更完整的参数和返回值请查看 Knife4j 页面：`http://localhost:8123/api/swagger-ui.html`。

## 测试与构建

后端全量测试：

```powershell
mvn test
```

指定后端测试类：

```powershell
mvn test -Dtest=AgentToolsTest
mvn test -Dtest=UserControllerTest
```

前端构建验证：

```powershell
cd .\ai-vue
npm.cmd run build
```

前端轻量 Node 测试示例：

```powershell
node .\src\stores\storyGuide.test.js
node .\src\router\pageHeaderBackTargets.test.js
```

## 开发约定

- Java 使用 4 空格缩进，类名使用 `PascalCase`，方法和字段使用 `camelCase`。
- Vue 单文件组件使用 `PascalCase` 命名，共享模块保持短小清晰。
- 修改 controller、tools、persistence、security、RAG 或 Agent 执行流时，应补充或更新测试。
- 不要提交真实 API Key、数据库密码、JWT Secret、OSS 密钥或 Pexels Key。
- `local-api-keys.txt`、`src/main/resources/application-local.yml`、`target/` 和 IDE 文件应保持本地化。

## 项目定位

- **用途**：AI Agent 学习、RAG 实践、多智能体产品原型、全栈应用演示。
- **阶段**：本地开发与功能迭代阶段，核心链路已覆盖前端、后端、RAG、工具调用和 MCP 扩展。
- **主要入口**：前端首页提供知心 Soul、灵语 Spark、极智 Core 三个智能体入口。

## 安全提示

本项目会读取本地模型、数据库、OSS、搜索和 MCP 相关配置。请确保密钥仅存在于本地环境变量或本地忽略文件中。提交 PR 或公开仓库前，应检查配置文件、日志、测试数据和截图中是否包含敏感信息。
