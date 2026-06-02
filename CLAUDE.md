# Commerce Context Engine — CLAUDE.md

> AI 어시스턴트가 이 프로젝트를 작업할 때 반드시 읽어야 하는 컨텍스트 문서.

---

## 프로젝트 한 줄 요약

**Spring AI MCP Server** 기반의 이커머스 도메인 지식 주입 서버.
Claude Code / Cursor 같은 AI 코딩 도구가 이커머스 백엔드를 개발할 때,
도메인 함정(오버셀링, 멱등성 누락, 분산 트랜잭션 미처리 등)을 자동으로 경고한다.

---

## 기술 스택

| 항목 | 값 |
|------|----|
| Language | Java 21 |
| Framework | Spring Boot 3.5.x (snapshot) |
| MCP | Spring AI MCP Server (`spring-ai-starter-mcp-server-webmvc`) |
| Spring AI BOM | 1.1.7 |
| Build | Gradle 8.x (Kotlin DSL) |
| 지식 저장 | YAML 파일 (`src/main/resources/knowledge/`) |

---

## 패키지 구조

```
com.commerce.context_engine          ← 루트 패키지 (밑줄 사용, 하이픈 X)
├── config/
│   ├── KnowledgeConfig.java         ← @PropertySource(YAML) + @EnableConfigurationProperties
│   ├── McpToolConfig.java           ← ToolCallbackProvider 빈 등록
│   └── YamlPropertySourceFactory.java
├── domain/inventory/
│   ├── InventoryKnowledge.java      ← (레거시 — 현재 미사용, 추후 제거 예정)
│   ├── InventoryKnowledgeItem.java  ← (레거시 — 현재 미사용, 추후 제거 예정)
│   └── InventoryKnowledgeProperties.java  ← @ConfigurationProperties("inventory")
├── domain/payment/
│   └── PaymentKnowledgeProperties.java    ← @ConfigurationProperties("payment")
├── service/
│   ├── InventoryKnowledgeService.java     ← 재고 지식 쿼리 로직
│   └── PaymentKnowledgeService.java       ← 결제 지식 쿼리 로직
└── tool/
    ├── InventoryContextTool.java          ← @Tool 6개 (재고)
    └── PaymentContextTool.java            ← @Tool 7개 (결제)
```

---

## 지식 데이터 위치

```
src/main/resources/knowledge/inventory.yml   ← 재고 도메인 지식 (MVP)
```

**YAML 구조**:
```yaml
inventory:
  items:
    - id: unique-id
      category: lifecycle | concurrency | idempotency | consistency | checklist
      title: "표시 제목"
      content: "지식 내용 (멀티라인 가능)"
      tags: [tag1, tag2]
```

새 지식 항목 추가는 이 YAML만 편집하면 된다. 코드 변경 불필요.

---

## MCP 도구 목록 (현재 노출 중)

**재고 도메인** (6개)

| 도구명 | 설명 |
|--------|------|
| `get_inventory_context` | 재고 전체 도메인 컨텍스트 |
| `get_concurrency_strategy` | 낙관락/비관락/분산락 선택 기준 |
| `get_saga_pattern` | Saga 보상 트랜잭션 가이드 |
| `get_idempotency_guide` | 재고 멱등성 키 설계 가이드 |
| `get_inventory_checklist` | 재고 AI 구현 실수 체크리스트 |
| `search_inventory_knowledge` | 재고 도메인 키워드 검색 |

**결제 도메인** (7개)

| 도구명 | 설명 |
|--------|------|
| `get_payment_webhook_guide` | PG사 웹훅 처리 가이드 |
| `get_duplicate_payment_guard` | 중복 결제 3중 방어 전략 |
| `get_network_cancellation_guide` | 망취소 처리 가이드 |
| `get_partial_refund_guide` | 부분 취소/환불 금액 역산 |
| `get_payment_idempotency_guide` | 결제 멱등성 키 설계 |
| `get_payment_checklist` | 결제 AI 구현 실수 체크리스트 |
| `search_payment_knowledge` | 결제 도메인 키워드 검색 |

---

## 개발 규칙

### 새 도메인 모듈 추가 순서

```
1. md/DOMAIN_KNOWLEDGE_REFERENCE.md 에 지식 정리
2. src/main/resources/knowledge/{domain}.yml 작성
3. domain/{domain}/{Domain}KnowledgeProperties.java 추가
4. config/KnowledgeConfig.java 에 @PropertySource + @EnableConfigurationProperties 추가
5. service/{Domain}KnowledgeService.java 작성
6. tool/{Domain}ContextTool.java 에 @Tool 메서드 추가
7. config/McpToolConfig.java 에 ToolCallbackProvider 빈 추가
8. 이 CLAUDE.md 의 도구 목록 업데이트
```

### 코드 스타일

- Lombok 사용 (`@Data`, `@RequiredArgsConstructor`, `@Slf4j`)
- `@Tool` description은 한국어로, 어떤 상황에 호출해야 하는지 명시
- `@ToolParam` description은 파라미터 예시까지 포함
- 서비스 메서드는 `format(List<Item>)` 헬퍼를 통해 일관된 마크다운 출력

### 금지 사항

- SnakeYAML raw Map 캐스팅 금지 (`@ConfigurationProperties`로 대체)
- `@PostConstruct`로 YAML 수동 파싱 금지 (Spring 바인딩 사용)
- 도구 description에 구현 세부사항 기술 금지 (호출 트리거 조건만 작성)

---

## 테스트

```bash
./gradlew test        # 전체 단위 + 통합 테스트 (15개)
```

**테스트 범위**:
- `InventoryKnowledgeServiceTest` — 7개: 카테고리 필터, 키워드 검색, 미등록 키워드 처리
- `InventoryContextToolTest` — 7개: 6개 도구 등록 확인, 각 도구 응답 내용 검증
- `PaymentKnowledgeServiceTest` — 8개: 웹훅/중복/망취소/환불/멱등성/체크리스트/검색
- `PaymentContextToolTest` — 7개: 각 결제 도구 응답 내용 검증
- `ContextEngineApplicationTests` — 1개: 컨텍스트 로드 확인

---

## npm 패키지 구조

```
npm/
├── package.json         ← 패키지명: commerce-context-mcp
└── bin/
    └── index.js         ← Java 체크 → JAR 다운로드 → STDIO 실행
```

**릴리즈 방법** (GitHub + npm 자동화):
```bash
git tag v0.0.1
git push origin v0.0.1
# → GitHub Actions가 자동으로:
#   1. JAR 빌드 (context-engine-0.0.1.jar)
#   2. GitHub Release 생성 + JAR 업로드
#   3. npm publish
```

**사용자 세팅 (최종)**:
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

**npm publish 사전 준비**:
1. npm 계정 생성: https://www.npmjs.com/signup
2. GitHub Secrets에 `NPM_TOKEN` 추가 (npm → Access Tokens → Automation 타입)

---

## 빌드 & 실행

```bash
# 빌드
./gradlew build

# Streamable HTTP 모드 실행 (Cursor 연결)
./gradlew bootRun

# STDIO 모드 실행 (Claude Code 연결)
java -jar build/libs/context-engine-0.0.1-SNAPSHOT.jar --spring.ai.mcp.server.stdio=true
```

## Claude Code 연결 설정

프로젝트 루트 `.claude/settings.json` (이미 존재):
```json
{
  "mcpServers": {
    "commerce-context": {
      "command": "C:\\Users\\김정민\\.jdks\\corretto-21.0.11\\bin\\java.exe",
      "args": [
        "-jar",
        "C:\\project\\context-engine\\build\\libs\\context-engine-0.0.1-SNAPSHOT.jar",
        "--spring.profiles.active=stdio"
      ]
    }
  }
}
```

**STDIO 프로파일 특징**:
- 로그가 `logs/context-engine-stdio.log` 파일로만 기록됨 (stdout 오염 없음)
- Tomcat이 랜덤 포트로 기동 (HTTP 포트 충돌 방지)
- Spring Boot 배너 비활성화

---

## 현재 로드맵 상태

| 단계 | 내용 | 상태 |
|------|------|------|
| 1단계 | MCP 서버 골격 | ✅ 완료 |
| 2단계 | 재고 도메인 모듈 구현 | ✅ 완료 |
| 3단계 | Claude Code 연동 테스트 | ✅ 완료 |
| 3-1단계 | Cursor SSE 연동 테스트 | ✅ 완료 |
| 4단계 | 주문/결제 정합성 모듈 구현 | ✅ 완료 |
| 5단계 | 정산 + 쿠폰 도메인 구현 | ✅ 완료 |
| 5-1단계 | npm 패키지 + GitHub Actions 릴리즈 자동화 | ✅ 완료 |
| 6단계 | 지식 저장소 DB 전환 + 관리자 API | 🔲 예정 |
| 7단계 | Beta 공개 (npm publish) | 🔲 예정 |
