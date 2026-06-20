# Repository Guidelines

## Project Structure & Module Organization

This repository contains a Spring Boot 3.4 backend at the root and a Vue 3 + Vite frontend in `ai-vue/`. Backend Java code lives in `src/main/java/com/yun/yunaiagent`, with focused packages for `agent`, `app`, `chat`, `config`, `controller`, `rag`, `security`, `tools`, and `user`. Backend resources, including `application.yml` and RAG documents, live in `src/main/resources`. Tests mirror the backend package layout under `src/test/java`. The independent image-search MCP server is in `yu-image-search-mcp-server/` with its own `pom.xml`. Supporting docs and scripts are in `docs/` and `scripts/`.

## Build, Test, and Development Commands

- `mvn test`: runs all backend JUnit tests.
- `mvn test -Dtest=AgentToolsTest`: runs one backend test class.
- `mvn spring-boot:run`: starts the backend on `http://localhost:8123/api`.
- `cd ai-vue; npm install`: installs frontend dependencies.
- `cd ai-vue; npm run dev`: starts the Vite development server.
- `cd ai-vue; npm run build`: creates the production frontend build.
- `cd yu-image-search-mcp-server; mvn spring-boot:run`: starts the MCP image-search server, typically on port `8127`.

Use JDK 21 for all Java modules.

## Coding Style & Naming Conventions

Java code uses 4-space indentation, Spring component annotations, constructor injection where practical, and descriptive package-level organization. Class names use `PascalCase`; methods, fields, and test methods use `camelCase`. Vue files use `PascalCase` component names such as `ChatRoom.vue`; shared frontend modules use concise names like `api/index.js` and `stores/userStore.js`. Keep secrets out of source and configuration defaults.

## Testing Guidelines

Backend tests use JUnit 5, Spring Boot Test, AssertJ patterns, H2 for test database coverage, and `@TempDir` for filesystem behavior. Name tests descriptively in English, for example `fileToolReadsAndWritesRealFiles`. Add or update tests when changing controllers, tools, persistence, security, RAG behavior, or agent execution flow.

## Commit & Pull Request Guidelines

Recent commits use short Chinese summary messages, for example `图片生成功能优化`. Keep commits focused and written as concise summaries. Pull requests should include a short description, test results such as `mvn test` or `npm run build`, linked issues when available, and screenshots for visible frontend changes.

## Security & Configuration Tips

Local secrets belong in environment variables or `local-api-keys.txt`; never commit real API keys, database passwords, JWT secrets, OSS credentials, or Pexels keys. `src/main/resources/application-local.yml`, `local-api-keys.txt`, `target/`, and IDE files are ignored and should remain local.
