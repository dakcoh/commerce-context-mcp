# Commerce Context Engine

Java/Spring 이커머스 백엔드 지식을 AI 코딩 도구에 연결하는 MCP 서버입니다.

Claude Code, Cursor 같은 MCP 클라이언트가 재고, 결제, 정산, 쿠폰, 포인트,
멤버십, 구독, 운영 안정성, Spring 구현 패턴을 조회해 더 안전한 백엔드 설계를
돕도록 만든 지식 엔진입니다.

## Features

- Spring AI MCP Server 기반 STDIO, HTTP/SSE 실행
- Java 17+ 지원
- YAML 기반 지식 관리
- 재고, 결제, 정산, 쿠폰/프로모션 도메인 지식
- 범용 이커머스 지식 20개
- Java/Spring 구현 지식 20개
- 지식 YAML 스키마 자동 검증
- npm 실행기 `commerce-context-mcp`

## Local Run

```powershell
.\gradlew.bat validateKnowledge --no-daemon
.\gradlew.bat bootJar --no-daemon
java -jar build\libs\context-engine-0.0.1-SNAPSHOT.jar --spring.profiles.active=stdio
```

## MCP Config

```json
{
  "mcpServers": {
    "commerce-context": {
      "command": "java",
      "args": [
        "-jar",
        "C:\\project\\context-engine\\build\\libs\\context-engine-0.0.1-SNAPSHOT.jar",
        "--spring.profiles.active=stdio"
      ]
    }
  }
}
```

After npm release:

```json
{
  "mcpServers": {
    "commerce-context": {
      "command": "npx",
      "args": ["-y", "commerce-context-mcp"]
    }
  }
}
```

## Docs

- [Architecture](docs/ARCHITECTURE.md)
- [Operations](docs/OPERATIONS.md)
- [Domain Knowledge Reference](docs/DOMAIN_KNOWLEDGE_REFERENCE.md)
- [External Usage Plan](docs/EXTERNAL_USAGE_PLAN.md)
- [Core Module Plan](docs/CORE_MODULE_PLAN.md)
