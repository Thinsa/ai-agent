# 安全加固 — 设计规范

**时间：** 2026-06-20  
**状态：** 已批准  
**范围：** 阶段 2 — 修复 4 个安全漏洞（严重 + 高优先级）

## 目标

1. JwtService：移除硬编码回退密钥，改为启动时快速失败
2. 3 个占位工具：默认禁用（文件读写、URL 下载、命令执行）
3. LocalEnvironmentFileInitializer：默认禁用明文密钥文件写入
4. AI 端点授权：关闭 `permitAll()`，强制认证

## 非目标

- 登录接口限流（推迟至阶段 3）
- Token 黑名单/撤销（推迟至阶段 3）
- RBAC 角色权限（推迟至阶段 3）
- 前端变更（所有端点 URL 和响应格式不变）

---

## 1. JwtService — 启动时快速失败

### 之前

```java
String resolvedSecret = secret == null || secret.length() < 32
        ? "local-development-secret-key-change-me-please"
        : secret;
```

### 之后

```java
if (secret == null || secret.length() < 32) {
    throw new IllegalStateException(
        "app.jwt.secret must be configured with at least 32 characters. " +
        "Set the APP_JWT_SECRET environment variable.");
}
this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
```

### 影响

- 生产环境（已配置）：零影响
- 开发环境：必须设置 `APP_JWT_SECRET` 环境变量
- 测试：已有 `@TestPropertySource` 设置密钥，零修改

---

## 2. 占位工具 — 默认禁用

### 3 个受影响工具

| 工具 | 属性开关 |
|------|----------|
| `FileOperationTool` | `app.tools.file-operation.enabled=true` |
| `ResourceDownloadTool` | `app.tools.resource-download.enabled=true` |
| `TerminalOperationTool` | `app.tools.terminal-execution.enabled=true` |

### 变更

每个工具添加 `@ConditionalOnProperty`：
```java
@Component
@ConditionalOnProperty(name = "app.tools.file-operation.enabled", havingValue = "true")
public class FileOperationTool implements AgentTool { ... }
```

`ToolRegistration` 使用 `ObjectProvider` 安全注入可选 Bean：
```java
@Bean
public ToolCallbackProvider agentToolCallbackProvider(
        ObjectProvider<FileOperationTool> fileOperationTool,
        WebSearchTool webSearchTool,
        WebScrapingTool webScrapingTool,
        ObjectProvider<ResourceDownloadTool> resourceDownloadTool,
        ObjectProvider<TerminalOperationTool> terminalOperationTool,
        PDFGenerationTool pdfGenerationTool,
        ImageGenerationTool imageGenerationTool,
        TerminateTool terminateTool
) {
    var builder = MethodToolCallbackProvider.builder()
            .toolObjects(webSearchTool, webScrapingTool,
                    pdfGenerationTool, imageGenerationTool, terminateTool);
    fileOperationTool.ifAvailable(builder::toolObjects);
    resourceDownloadTool.ifAvailable(builder::toolObjects);
    terminalOperationTool.ifAvailable(builder::toolObjects);
    return builder.build();
}
```

### 影响

- 默认：3 个工具不注册，AI Agent 无法调用
- 启用：设置对应属性为 `true` 后恢复

---

## 3. LocalEnvironmentFileInitializer — 默认禁用

### 变更

```java
@Component
@ConditionalOnProperty(name = "app.env-file.enabled", havingValue = "true")
public class LocalEnvironmentFileInitializer implements ApplicationRunner {
    // ...
    @Override
    public void run(ApplicationArguments args) {
        log.warn("SECURITY: Writing secrets to local-api-keys.txt in plaintext. " +
                 "Disable with app.env-file.enabled=false in production.");
        // 原有逻辑不变
    }
}
```

### 影响

- 默认：密钥文件不再写入
- .gitignore 已有 `local-api-keys.txt`（无需修改）

---

## 4. AI 端点授权 — 强制认证

### 4a. JwtAuthenticationFilter — 新增强查询参数 Token 支持

```java
@Override
protected void doFilterInternal(HttpServletRequest request, ...) {
    String token = null;
    
    // 1. Authorization header（原有逻辑）
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
        token = header.substring(7);
    }
    
    // 2. 回退到 ?token= 查询参数（新增，支持 SSE EventSource）
    if (token == null) {
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.isBlank()) {
            token = tokenParam;
        }
    }
    
    if (token != null) {
        try {
            String username = jwtService.parseUsername(token);
            // 设置 SecurityContext（原有逻辑不变）
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }
    }
    filterChain.doFilter(request, response);
}
```

### 4b. SecurityConfig — 关闭 permitAll()

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
    .requestMatchers("/user/register", "/user/login", "/user/token/validate",
            "/health", "/swagger-ui.html", "/swagger-ui/**",
            "/v3/api-docs/**").permitAll()
    .requestMatchers("/user/current", "/user/profile", "/user/logout",
            "/backgrounds/**", "/story-saves/**",
            "/knowledge-documents/**").authenticated()
    .requestMatchers("/ai/**").authenticated()     // 新增：AI 端点需要认证
    .anyRequest().authenticated()                   // 之前是 .permitAll()
)
```

### 影响

- SSE 端点：前端已通过 `EventSource("...?token=...")` 传递 token，JwtAuthenticationFilter 现在可解析
- 非 SSE 端点：前端已通过 `Authorization: Bearer` header 传递 token
- 无认证请求：返回 401（之前是 200）

---

## 文件变更

| 文件 | 操作 |
|------|--------|
| `security/JwtService.java` | 修改：移除回退密钥，添加启动校验 |
| `tools/FileOperationTool.java` | 修改：添加 `@ConditionalOnProperty` |
| `tools/ResourceDownloadTool.java` | 修改：添加 `@ConditionalOnProperty` |
| `tools/TerminalOperationTool.java` | 修改：添加 `@ConditionalOnProperty` |
| `tools/ToolRegistration.java` | 修改：使用 `ObjectProvider` 注册可选工具 |
| `config/LocalEnvironmentFileInitializer.java` | 修改：添加 `@ConditionalOnProperty` + 警告日志 |
| `security/JwtAuthenticationFilter.java` | 修改：添加查询参数 token 回退 |
| `config/SecurityConfig.java` | 修改：`/ai/**` 和默认规则改为 `.authenticated()` |

---

## 验证

1. `mvn test` — 所有 51 个测试通过（可能需要更新 `@TestPropertySource`）
2. 启动时无 `APP_JWT_SECRET`：启动失败，提示明确错误信息
3. 启动时有 `APP_JWT_SECRET`：正常启动，3 个占位工具未注册
4. 无认证请求 `/ai/chat/sse`：返回 401
5. 携带 token 请求 `/ai/chat/sse`：正常返回 SSE 流
