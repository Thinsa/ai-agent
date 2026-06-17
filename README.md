# Yun AI Agent

Spring Boot + Spring AI 后端，配套 Vue 3 前端。当前版本已接入 DashScope 聊天、JWT 用户系统、PostgreSQL/PGVector RAG 入口、工具调用、MCP Client 入口和前端真实 API 登录态。

## 本地私密配置

敏感配置只从环境变量读取，不写入 `application.yml` 的默认账号密码。项目启动时会把已存在的本地变量合并到 `local-api-keys.txt`，同名变量优先级为：

```text
当前进程环境变量 > Windows 用户环境变量 > local-api-keys.txt 原值
```

支持的变量：

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
APP_MCP_CLIENT_ENABLED
APP_MCP_IMAGE_SEARCH_URL
PEXELS_API_KEY
```

`local-api-keys.txt` 是本地私密文件，不应提交或分享。PowerShell 注入示例：

```powershell
Get-Content .\local-api-keys.txt | ForEach-Object {
  if ($_ -match '^([^=]+)=(.*)$') {
    [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
  }
}
```

## 运行

需要 JDK 21、Maven、PostgreSQL + pgvector，以及 DashScope API Key。

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

## 前端

前端目录：

```text
D:\html+css+js\ai-vue
```

```powershell
cd D:\html+css+js\ai-vue
npm install
npm run dev
npm run build
```

## MCP image search server

The image search MCP server is an independent project under `yu-image-search-mcp-server`.
Start it before enabling the main backend MCP client.

```powershell
cd .\yu-image-search-mcp-server
$env:PEXELS_API_KEY='your-pexels-key'
mvn spring-boot:run
```

Then start the main backend with MCP client enabled:

```powershell
$env:APP_MCP_CLIENT_ENABLED='true'
$env:APP_MCP_IMAGE_SEARCH_URL='http://localhost:8127'
mvn spring-boot:run
```

Keep `PEXELS_API_KEY` in local environment variables or `local-api-keys.txt`; do not commit or share real keys.

前端开发环境请求 `http://localhost:8123/api`，已使用真实后端接口实现注册、登录、刷新恢复登录态、资料更新和登出。
