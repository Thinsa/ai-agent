# LinkMind / Yun AI Agent

LinkMind 是一个基于 Spring Boot 3.4、Spring AI 和 Vue 3 的 AI Agent 应用。后端提供聊天、RAG、工具调用、MCP Client、JWT 用户系统、聊天历史、故事保存、背景图配置和头像上传等能力；前端位于 `ai-vue/`，通过真实后端 API 完成注册、登录、会话和用户资料相关流程。

## 功能概览

- AI 对话：支持普通聊天、SSE 流式输出、恋爱咨询应用、故事生成和 Manus Agent。
- RAG 检索增强：基于 PostgreSQL + pgvector，支持文档加载、向量检索和查询改写。
- Agent 工具调用：包含文件操作、终端操作、网页抓取、Web 搜索、资源下载、PDF 生成、图片生成和终止工具。
- MCP 扩展：主后端可作为 MCP Client 接入独立的图片搜索 MCP Server。
- 用户系统：支持注册、登录、JWT 鉴权、当前用户、Token 校验、资料更新、头像上传和退出登录。
- 数据持久化：使用 Spring Data JPA 保存用户、聊天会话、聊天消息、故事收藏和用户背景图设置。

## 技术栈

### 后端

- Java 21
- Spring Boot 3.4.4
- Spring AI 1.0.0
- Spring AI Alibaba DashScope
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL + pgvector
- Knife4j / springdoc-openapi
- JUnit 5、Spring Boot Test、H2

### 前端

- Vue 3
- Vite 4
- Vue Router
- Axios
- @vueuse/head

## 项目结构

```text
.
├── ai-vue/                         # Vue 3 + Vite 前端
├── docs/                           # 项目文档
├── scripts/                        # 辅助脚本
├── src/
│   ├── main/
│   │   ├── java/com/yun/yunaiagent/
│   │   │   ├── agent/              # Agent 基类、ReAct、工具调用和 YuManus
│   │   │   ├── app/                # 业务 AI 应用
│   │   │   ├── background/         # 用户背景图设置
│   │   │   ├── chat/               # 聊天会话与消息历史
│   │   │   ├── common/             # 通用工具
│   │   │   ├── config/             # CORS、安全、本地环境变量加载
│   │   │   ├── controller/         # REST / SSE 接口
│   │   │   ├── rag/                # RAG 文档加载、检索、查询改写
│   │   │   ├── security/           # JWT 鉴权
│   │   │   ├── storysave/          # 故事保存
│   │   │   ├── tools/              # Agent 可调用工具
│   │   │   └── user/               # 用户、头像和 OSS 配置
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       └── document/           # RAG 文档资源
│   └── test/java/                  # 后端测试
├── yu-image-search-mcp-server/     # 独立图片搜索 MCP Server
├── pom.xml                         # 后端 Maven 项目
└── README.md
```

## 环境要求

- JDK 21
- Maven 3.9+
- Node.js 18+ 与 npm
- PostgreSQL，并安装 pgvector 扩展
- DashScope API Key
- 可选：Pexels API Key，用于图片搜索 MCP Server
- 可选：阿里云 OSS 配置，用于头像或对象存储能力

## 本地配置

敏感配置只应保存在环境变量、`src/main/resources/application-local.yml` 或 `local-api-keys.txt` 中，不要提交真实密钥。

项目启动时会读取本地环境变量，并可合并到 `local-api-keys.txt`。同名变量优先级为：

```text
当前进程环境变量 > Windows 用户环境变量 > local-api-keys.txt 原值
```

常用环境变量：

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

PowerShell 临时注入 `local-api-keys.txt` 的示例：

```powershell
Get-Content .\local-api-keys.txt | ForEach-Object {
  if ($_ -match '^([^=]+)=(.*)$') {
    [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
  }
}
```

## 后端启动

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

## 前端启动

```powershell
cd .\ai-vue
npm install
npm run dev
```

Vite 默认运行在：

- Dev server: `http://localhost:3000`
- API proxy: `/api` -> `http://localhost:8123`

构建生产包：

```powershell
cd .\ai-vue
npm run build
```

## MCP 图片搜索服务

图片搜索 MCP Server 是独立 Maven 项目，位于 `yu-image-search-mcp-server/`。如果要启用主后端的 MCP Client，请先启动该服务。

```powershell
cd .\yu-image-search-mcp-server
$env:PEXELS_API_KEY='your-pexels-key'
mvn spring-boot:run
```

再启动主后端：

```powershell
$env:APP_MCP_CLIENT_ENABLED='true'
$env:APP_MCP_IMAGE_SEARCH_URL='http://localhost:8127'
mvn spring-boot:run
```

## 常用接口入口

后端统一使用 `/api` 作为上下文路径。

- `GET /api/health`：健康检查。
- `/api/user/*`：注册、登录、当前用户、Token 校验、资料更新、头像上传和退出登录。
- `/api/ai/*`：AI 聊天、SSE、RAG、工具调用、MCP 和 Manus Agent。
- `/api/ai/history/*`：聊天会话与消息历史。
- `/api/story-saves/*`：故事保存、列表和删除。
- `/api/backgrounds/*`：用户背景图配置。

更完整的接口参数请查看 Knife4j 页面：`http://localhost:8123/api/swagger-ui.html`。

## 测试

运行全部后端测试：

```powershell
mvn test
```

运行指定测试类：

```powershell
mvn test -Dtest=AgentToolsTest
mvn test -Dtest=UserControllerTest
```

前端构建验证：

```powershell
cd .\ai-vue
npm run build
```

## 开发注意事项

- Java 代码使用 4 空格缩进，类名使用 `PascalCase`，方法和字段使用 `camelCase`。
- 测试类放在 `src/test/java` 下，并尽量与被测包结构保持一致。
- 修改控制器、工具、持久化、安全、RAG 或 Agent 执行流时，应补充或更新测试。
- `local-api-keys.txt`、`src/main/resources/application-local.yml`、`target/` 和 IDE 文件不应提交。
- 不要提交真实 API Key、数据库密码、JWT Secret、OSS 密钥或 Pexels Key。
