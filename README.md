# Commerce Context MCP

Java/Spring 이커머스 백엔드 지식을 Claude Code, Cursor 같은 AI 코딩 도구에 연결하는 MCP 서버입니다.

```powershell
npx -y commerce-context-mcp doctor
```

처음 사용하는 경우 [npm 초보자 가이드](npm/README.md)를 먼저 보세요.  
일반 사용자는 JAR 파일을 직접 다운로드하거나 경로를 설정할 필요가 없습니다.

## English Summary

Commerce Context MCP is an MCP server that provides Java/Spring ecommerce backend knowledge to AI coding tools such as Claude Code and Cursor.

## 제공하는 지식

- 재고: 오버셀링, 재고 예약, Saga, 멱등성
- 결제: 웹훅 중복, 결제 멱등성, 망취소, 부분 환불
- 정산: 정산 시점, 공제, 대사, 배치 재처리
- 쿠폰/프로모션: 발급 동시성, 할인 계산, 프로모션 규칙
- Java/Spring 구현: 트랜잭션, JPA, 페이징, 예외 경계

## MCP 설정

클라이언트별 설정 파일 위치는 [npm 초보자 가이드](npm/README.md)를 참고하세요.

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

처음 실행하면 npm 실행기가 현재 npm 패키지 버전에 맞는 GitHub Release JAR를 내려받고,
사용자 홈 디렉터리의 `.commerce-context-mcp` 캐시에 저장한 뒤 STDIO MCP 서버로 실행합니다.

## 연결 후 테스트 질문

- "Spring Boot 쇼핑몰 주문 API 만들 때 빠뜨리기 쉬운 점 알려줘"
- "재고 차감 로직에서 오버셀링을 막는 기준 알려줘"
- "결제 웹훅 처리에서 중복 결제를 어떻게 막아?"
- "쿠폰 발급에서 Redis로 동시성을 제어할 때 주의할 점 알려줘"
- "Spring Boot에서 cursor pagination과 idempotent POST를 어떻게 설계하면 좋아?"

## 필요 환경

- Node.js 16 이상
- Java 17 이상
- Claude Code, Cursor, Claude Desktop 등 MCP 클라이언트

## 개발자용 실행

공개 사용자는 위의 `npx` 설정을 사용하면 됩니다.  
아래 명령은 저장소를 내려받아 직접 개발하거나 릴리즈 JAR를 검증할 때 사용합니다.

```powershell
.\gradlew.bat validateKnowledge --no-daemon
.\gradlew.bat bootJar --no-daemon
Get-ChildItem build\libs\*.jar
java -jar build\libs\<jar-file> --spring.profiles.active=stdio
```

## 문서

- [npm 초보자 가이드](npm/README.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Operations](docs/OPERATIONS.md)
- [Domain Knowledge Reference](docs/DOMAIN_KNOWLEDGE_REFERENCE.md)
- [Core Module Plan](docs/CORE_MODULE_PLAN.md)

## License

[MIT](LICENSE)
