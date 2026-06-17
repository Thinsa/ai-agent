# Yun AI Agent API 文档

Base URL:

```text
http://localhost:8123/api
```

Swagger / Knife4j:

```text
http://localhost:8123/api/swagger-ui.html
```

认证方式：`Authorization: Bearer <token>`。

## 用户接口

### POST `/user/register`

注册用户，用户名唯一。重复用户名返回 `409`。

请求：

```json
{
  "username": "alice",
  "password": "password123",
  "displayName": "Alice",
  "email": "alice@example.com"
}
```

响应：用户信息，不返回密码。

### POST `/user/login`

登录成功返回 JWT 和用户信息。用户名不存在或密码错误返回 `401`。

请求：

```json
{
  "username": "alice",
  "password": "password123"
}
```

响应：

```json
{
  "token": "<jwt>",
  "user": {
    "id": 1,
    "username": "alice",
    "displayName": "Alice",
    "email": "alice@example.com",
    "role": "AI 应用体验官",
    "bio": "",
    "createdAt": "...",
    "updatedAt": "..."
  }
}
```

### GET `/user/current`

需要 Bearer Token。返回当前登录用户。

### PUT `/user/profile`

需要 Bearer Token。更新当前用户资料。

请求：

```json
{
  "displayName": "Alice",
  "email": "alice@example.com",
  "role": "AI 应用体验官",
  "bio": "正在探索 AI 应用。"
}
```

### POST `/user/logout`

无状态登出。后端返回成功，前端清理本地 token。

## AI 恋爱大师

所有接口路径都带 `/ai` 前缀。

| 方法 | 路径 | 响应 | 说明 |
|---|---|---|---|
| GET | `/ai/love_app/chat/sync` | text | 同步聊天 |
| GET | `/ai/love_app/chat/sse` | `text/event-stream` | 流式聊天，前端正在使用 |
| GET | `/ai/love_app/chat/server_sent_event` | `text/event-stream` | 标准 ServerSentEvent 包装 |
| GET | `/ai/love_app/chat/sse_emitter` | `text/event-stream` | MVC `SseEmitter` 包装 |
| GET | `/ai/love_app/chat/report` | json | 结构化恋爱建议报告 |
| GET | `/ai/love_app/chat/rag` | text | PGVector/文档 RAG 问答 |
| GET | `/ai/love_app/chat/tools` | text | ChatClient 工具调用 |
| GET | `/ai/love_app/chat/mcp` | text | MCP 工具调用入口 |

通用 query 参数：

| 参数 | 类型 | 说明 |
|---|---|---|
| `message` | string | 用户消息 |
| `chatId` | string | 会话 ID，用于隔离 20 条窗口记忆 |

SSE 示例：

```powershell
curl.exe -N "http://localhost:8123/api/ai/love_app/chat/sse?message=%E4%BD%A0%E5%A5%BD&chatId=love-001"
```

RAG 示例：

```powershell
curl.exe "http://localhost:8123/api/ai/love_app/chat/rag?message=%E6%80%8E%E4%B9%88%E5%81%A5%E5%BA%B7%E6%B2%9F%E9%80%9A&chatId=rag-001"
```

## YuManus 超级智能体

### GET `/ai/manus/chat`

SSE 执行智能体任务。每次请求创建一个新的 YuManus 实例，最多 20 步，调用终止工具或达到步数上限后结束。

参数：

| 参数 | 类型 | 说明 |
|---|---|---|
| `message` | string | 用户目标 |

示例：

```powershell
curl.exe -N "http://localhost:8123/api/ai/manus/chat?message=%E5%B8%AE%E6%88%91%E7%94%9F%E6%88%90%E4%B8%80%E4%BB%BD%E8%AE%A1%E5%88%92"
```

## 前端 API 对应

文件：`D:\html+css+js\ai-vue\src\api\index.js`

| 前端函数 | 后端接口 |
|---|---|
| `register(payload)` | `POST /user/register` |
| `login(payload)` | `POST /user/login` |
| `getCurrentUser()` | `GET /user/current` |
| `updateProfile(payload)` | `PUT /user/profile` |
| `logout()` | `POST /user/logout` |
| `chatWithLoveApp(message, chatId)` | `GET /ai/love_app/chat/sse` |
| `chatWithManus(message)` | `GET /ai/manus/chat` |

前端 token 存储在 `localStorage` 的 `yun_ai_token`。Axios request interceptor 会自动注入 Bearer Token。

## 本地配置提醒

运行前请先配置：

```text
DASHSCOPE_API_KEY
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
APP_JWT_SECRET
```

可选：

```text
SEARCH_API_API_KEY
APP_JWT_EXPIRATION
APP_TOOLS_WORKSPACE
APP_TOOLS_COMMAND_TIMEOUT_SECONDS
APP_TOOLS_MAX_OUTPUT_CHARS
```

`local-api-keys.txt` 是本地私密文件，不应提交或分享。
