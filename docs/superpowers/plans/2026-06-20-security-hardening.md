# Security Hardening — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix 4 security vulnerabilities: remove JWT hardcoded fallback, disable 3 placeholder tools by default, gate plaintext secrets file behind a property, and require authentication on AI endpoints.

**Architecture:** Each fix is independent. JwtAuthenticationFilter gains query-param token fallback to support SSE EventSource auth. ToolRegistration switches to ObjectProvider for optional tool beans. SecurityConfig tightens from `.anyRequest().permitAll()` to explicit `.authenticated()` rules.

**Tech Stack:** Spring Boot 3.4.4, Spring Security, Spring AI 1.0.0, JUnit 5

---

### Task 1: JwtService — remove hardcoded fallback, fail-fast on weak secret

**Files:**
- Modify: `src/main/java/com/yun/yunaiagent/security/JwtService.java:26-28`

- [ ] **Step 1: Replace fallback with validation**

Read the file, then replace lines 26-28:
```java
        String resolvedSecret = secret == null || secret.length() < 32
                ? "local-development-secret-key-change-me-please"
                : secret;
        this.secretKey = Keys.hmacShaKeyFor(resolvedSecret.getBytes(StandardCharsets.UTF_8));
```

With:
```java
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret must be configured with at least 32 characters. " +
                    "Set the APP_JWT_SECRET environment variable.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
```

- [ ] **Step 2: Verify compilation** — `mvn compile -q` → BUILD SUCCESS
- [ ] **Step 3: Run tests** — `mvn test -q` → all 51 pass (tests already provide secret via `@TestPropertySource`)
- [ ] **Step 4: Commit** — `git commit -m "fix: remove JWT hardcoded fallback, fail-fast on weak secret"`

---

### Task 2: Disable 3 placeholder tools by default

**Files:**
- Modify: `src/main/java/com/yun/yunaiagent/tools/FileOperationTool.java` — add `@ConditionalOnProperty`
- Modify: `src/main/java/com/yun/yunaiagent/tools/ResourceDownloadTool.java` — add `@ConditionalOnProperty`
- Modify: `src/main/java/com/yun/yunaiagent/tools/TerminalOperationTool.java` — add `@ConditionalOnProperty`
- Modify: `src/main/java/com/yun/yunaiagent/tools/ToolRegistration.java` — use `ObjectProvider`

- [ ] **Step 1: Add @ConditionalOnProperty to FileOperationTool**

Add import `import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;`
Add annotation above `@Component`:
```java
@Component
@ConditionalOnProperty(name = "app.tools.file-operation.enabled", havingValue = "true")
public class FileOperationTool implements AgentTool {
```

- [ ] **Step 2: Same for ResourceDownloadTool**

Add same import and annotation:
```java
@Component
@ConditionalOnProperty(name = "app.tools.resource-download.enabled", havingValue = "true")
public class ResourceDownloadTool implements AgentTool {
```

- [ ] **Step 3: Same for TerminalOperationTool**

Add same import and annotation:
```java
@Component
@ConditionalOnProperty(name = "app.tools.terminal-execution.enabled", havingValue = "true")
public class TerminalOperationTool implements AgentTool {
```

- [ ] **Step 4: Update ToolRegistration to use ObjectProvider**

Replace the current `agentToolCallbackProvider` bean method. Read the file first, then replace the method signature and body:

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
                .toolObjects(
                        webSearchTool,
                        webScrapingTool,
                        pdfGenerationTool,
                        imageGenerationTool,
                        terminateTool
                );
        fileOperationTool.ifAvailable(builder::toolObjects);
        resourceDownloadTool.ifAvailable(builder::toolObjects);
        terminalOperationTool.ifAvailable(builder::toolObjects);
        return builder.build();
    }
```

Add import: `import org.springframework.beans.factory.ObjectProvider;`

- [ ] **Step 5: Verify compilation** — `mvn compile -q` → BUILD SUCCESS
- [ ] **Step 6: Run tests** — `mvn test -q` → all 51 pass (tools not used in most tests)
- [ ] **Step 7: Commit** — `git commit -m "fix: disable placeholder tools by default, gate behind feature flags"`

---

### Task 3: Gate LocalEnvironmentFileInitializer behind property

**Files:**
- Modify: `src/main/java/com/yun/yunaiagent/config/LocalEnvironmentFileInitializer.java`

- [ ] **Step 1: Add @ConditionalOnProperty and security warning**

Add imports:
```java
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
```

Add annotation above `@Component`:
```java
@Component
@ConditionalOnProperty(name = "app.env-file.enabled", havingValue = "true")
```

In the `run()` method, add a warning as the first line inside the try block (after line 26):
```java
log.warn("SECURITY: Writing secrets to local-api-keys.txt in plaintext. " +
         "Set app.env-file.enabled=false to disable this feature.");
```

- [ ] **Step 2: Verify compilation** — `mvn compile -q` → BUILD SUCCESS
- [ ] **Step 3: Run tests** — `mvn test -q` → all pass (no production code test depends on this initializer)
- [ ] **Step 4: Commit** — `git commit -m "fix: gate LocalEnvironmentFileInitializer behind app.env-file.enabled property"`

---

### Task 4: Add query-param token support to JwtAuthenticationFilter

**Files:**
- Modify: `src/main/java/com/yun/yunaiagent/security/JwtAuthenticationFilter.java:26-38`

- [ ] **Step 1: Add query-param token fallback**

Read the file, then replace the `doFilterInternal` method body (lines 27-38):

```java
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String token = null;

        // 1. Authorization header (existing behavior)
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        // 2. Fallback to ?token= query parameter (supports SSE EventSource)
        if (token == null) {
            String tokenParam = request.getParameter("token");
            if (tokenParam != null && !tokenParam.isBlank()) {
                token = tokenParam;
            }
        }

        if (token != null) {
            try {
                String username = jwtService.parseUsername(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, List.of());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
```

The method body is self-contained — replaces everything between `doFilterInternal(...) {` and the closing `}`.

- [ ] **Step 2: Verify compilation** — `mvn compile -q` → BUILD SUCCESS
- [ ] **Step 3: Commit** — `git commit -m "feat: add query-param token fallback to JwtAuthenticationFilter for SSE"`

---

### Task 5: Tighten SecurityConfig — require auth on /ai/** and default

**Files:**
- Modify: `src/main/java/com/yun/yunaiagent/config/SecurityConfig.java:53-56`

- [ ] **Step 1: Replace .anyRequest().permitAll() with authenticated rules**

Read the file, then replace lines 53-56:
```java
                        .requestMatchers("/user/current", "/user/profile", "/user/logout", "/backgrounds/**", "/story-saves/**", "/knowledge-documents/**").authenticated()
                        .anyRequest().permitAll()
```

With:
```java
                        .requestMatchers("/user/current", "/user/profile", "/user/logout",
                                "/backgrounds/**", "/story-saves/**",
                                "/knowledge-documents/**").authenticated()
                        .requestMatchers("/ai/**").authenticated()
                        .anyRequest().authenticated()
```

- [ ] **Step 2: Verify compilation** — `mvn compile -q` → BUILD SUCCESS
- [ ] **Step 3: Run full test suite** — `mvn test -q`

Some controller tests may fail because they don't send auth headers. Check:

- `StoryControllerTest` — may need to pass token param
- `AiControllerMcpTest` (now `ManusControllerTest`) — may need token
- `StorySaveControllerTest` — already uses `registerAndLogin()`, should pass
- `BackgroundControllerTest` — already uses `registerAndLogin()`, should pass
- `ChatHistoryControllerTest` — already uses `registerAndLogin()`, should pass

If any test fails with 401, add `?token=` to the request URL or add `Authorization` header.

- [ ] **Step 4: Fix any test failures, re-run until all 51 pass**
- [ ] **Step 5: Commit** — `git commit -m "fix: require authentication on /ai/** endpoints, remove permitAll()"`

---

### Task 6: Final verification

- [ ] **Step 1: Full test suite** — `mvn test` → 51 tests, 0 failures
- [ ] **Step 2: Spot-check compilation with no JWT secret** — temporarily rename env var and verify startup fails with clear message
- [ ] **Step 3: Commit any remaining tweaks**
