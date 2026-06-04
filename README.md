# Commerce Context Engine

> Java/Spring ecommerce backend knowledge MCP server for AI coding tools.
> AI 코딩 도구에 Java/Spring 이커머스 백엔드 지식을 연결하는 MCP 서버입니다.

AI에게 "쇼핑몰 주문 API 만들어줘"라고 시키면 컨트롤러와 엔티티는 곧잘 만들어줍니다.
그런데 실제 이커머스 백엔드에서 중요한 재고 예약, 결제 웹훅 중복, 망취소,
부분 환불, 정산 공제, 포인트 원장 같은 내용은 자주 빠집니다.

Commerce Context Engine은 이런 이커머스 백엔드 함정을 MCP 도구로 AI에게 주입하는 서버입니다.
Claude Code, Cursor 같은 MCP 클라이언트에서 Java/Spring 이커머스 지식을 불러와
설계와 코드 리뷰에 참고할 수 있게 합니다.

단순 코드 생성보다 이커머스 백엔드 설계 검토, 구현 체크리스트, 장애 예방 지식을
AI 개발 도구에 연결하는 데 초점을 둡니다.

## English Summary

Commerce Context Engine is an MCP server that provides Java/Spring ecommerce backend knowledge to AI coding tools such as Claude Code and Cursor.
It focuses on practical risks such as overselling, payment idempotency, webhook duplication, refund consistency, settlement reconciliation, and Spring implementation pitfalls.

## 제공하는 것

- npm 패키지 `commerce-context-mcp`
- Claude Code / Cursor용 MCP 설정
- YAML 기반 지식 관리
- 재고, 결제, 정산, 쿠폰/프로모션 지식
- 범용 이커머스 지식 20개
- Java/Spring 구현 지식 20개
- 지식 YAML 스키마 자동 검증
- 오버셀링, 결제 멱등성, 웹훅 중복, 정산 정합성 같은 실무 리스크 체크리스트
- Java 17+ 지원
- Spring AI MCP Server 기반 STDIO 실행

## 필요 환경

- Node.js 16 이상
- Java 17 이상
- Claude Code, Cursor 등 MCP를 지원하는 클라이언트

## 빠른 확인

```powershell
npx commerce-context-mcp doctor
```

`doctor`는 Node.js 버전, Java 버전, 로컬 JAR 캐시, GitHub Release 다운로드 URL을 확인합니다.

## MCP 설정

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

처음 실행하면 npm 실행기가 GitHub Release에서 `context-engine-0.0.1.jar`를 내려받고,
사용자 캐시에 저장한 뒤 STDIO MCP 서버로 실행합니다.

## 예시 질문

- "Spring Boot 쇼핑몰 주문 API 만들 때 빠뜨리기 쉬운 점 알려줘"
- "재고 차감 로직에서 오버셀링을 막는 기준 알려줘"
- "결제 웹훅 처리에서 중복 결제를 어떻게 막아?"
- "이커머스에서 포인트와 멤버십 등급을 설계할 때 주의할 점 알려줘"
- "Spring Boot에서 cursor pagination과 idempotent POST를 어떻게 설계하면 좋아?"

## 개발자용 실행

공개 사용자는 위의 `npx` 설정을 사용하면 됩니다.
아래 명령은 저장소를 내려받아 직접 개발하거나 릴리즈 JAR를 검증할 때 사용합니다.

```powershell
.\gradlew.bat validateKnowledge --no-daemon
.\gradlew.bat bootJar --no-daemon
java -jar build\libs\context-engine-0.0.1-SNAPSHOT.jar --spring.profiles.active=stdio
```

## 문서

- [Architecture](docs/ARCHITECTURE.md)
- [Operations](docs/OPERATIONS.md)
- [Domain Knowledge Reference](docs/DOMAIN_KNOWLEDGE_REFERENCE.md)
- [Core Module Plan](docs/CORE_MODULE_PLAN.md)

## License

[MIT](LICENSE)
