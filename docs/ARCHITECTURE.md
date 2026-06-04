# Commerce Context Engine — Architecture

> 상세 아키텍처 문서. 시스템 구조, 계층 책임, 데이터 흐름을 기술한다.

---

## 전체 구조

```
┌──────────────────────────────────────────────────────┐
│                   AI 코딩 도구                         │
│  ┌──────────────────┐   ┌──────────────────────────┐  │
│  │   Claude Code    │   │         Cursor           │  │
│  │   (STDIO 모드)   │   │   (SSE / HTTP 모드)      │  │
│  └────────┬─────────┘   └─────────────┬────────────┘  │
└───────────┼─────────────────────────────┼─────────────┘
            │       MCP Protocol          │
            └──────────────┬──────────────┘
                           ▼
┌──────────────────────────────────────────────────────────┐
│              Commerce Context Engine                     │
│              (Spring Boot 3.5 + Spring AI MCP)           │
│                                                          │
│  ┌───────────────────────────────────────────────────┐   │
│  │              Tool Layer  (@Tool × 31)             │   │
│  │                                                   │   │
│  │  InventoryContextTool    × 6  (재고)              │   │
│  │  PaymentContextTool      × 7  (결제)              │   │
│  │  SettlementContextTool   × 6  (정산)              │   │
│  │  CouponContextTool       × 6  (쿠폰/프로모션)     │   │
│  │  CommerceContextTool     × 3  (범용 이커머스)     │   │
│  │  SpringCommerceContextTool × 3 (Java Spring)     │   │
│  └────────────────────┬──────────────────────────────┘   │
│                       │                                  │
│  ┌────────────────────▼──────────────────────────────┐   │
│  │               Service Layer                       │   │
│  │  {Domain}KnowledgeService × 6                    │   │
│  │  • category 기반 필터링                            │   │
│  │  • 키워드 검색 (title + content + tags)            │   │
│  │  • 마크다운 포맷 변환                              │   │
│  └────────────────────┬──────────────────────────────┘   │
│                       │                                  │
│  ┌────────────────────▼──────────────────────────────┐   │
│  │             Knowledge Layer                       │   │
│  │  {Domain}KnowledgeProperties × 6                 │   │
│  │  (@ConfigurationProperties)                      │   │
│  │                                                   │   │
│  │  inventory.yml      payment.yml                   │   │
│  │  settlement.yml     coupon.yml                    │   │
│  │  commerce.yml       spring-commerce.yml           │   │
│  └───────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────┘
```

---

## 계층별 책임

### Tool Layer (`tool/`)

- Spring AI `@Tool` 어노테이션으로 MCP 프로토콜에 메서드를 노출한다.
- **책임**: 도구 정의, description 작성 (AI가 언제 이 도구를 호출할지 결정하는 핵심)
- **금지**: 비즈니스 로직 직접 구현. 서비스에 위임만 한다.

### Service Layer (`service/`)

- 지식 쿼리, 필터링, 포맷팅 담당.
- **책임**: category 기반 필터링, 키워드 검색, 마크다운 변환
- **확장 포인트**: 추후 DB 전환 시 이 계층만 교체하면 된다.

### Knowledge Layer (`domain/`, `resources/knowledge/`)

- `@ConfigurationProperties`로 YAML을 타입-안전하게 바인딩한다.
- **책임**: 지식 데이터 모델 정의, YAML → Java 객체 변환
- **확장**: 새 도메인 추가 시 새 YAML 파일 + 새 Properties 클래스 추가

### Config Layer (`config/`)

| 클래스 | 역할 |
|--------|------|
| `KnowledgeConfig` | YAML `@PropertySource` 등록, `@EnableConfigurationProperties` |
| `McpToolConfig` | `MethodToolCallbackProvider`로 `@Tool` 빈을 MCP 서버에 등록 |
| `YamlPropertySourceFactory` | `@PropertySource`가 YAML을 읽을 수 있도록 하는 팩토리 |

---

## 데이터 흐름

```
개발자 요청: "재고 차감 로직 만들어줘"
        │
        ▼
AI 코딩 도구 (Claude Code / Cursor)
  → 요청 분석: "재고" 키워드 감지
  → MCP 도구 호출 결정
        │
        ▼ MCP Protocol (STDIO or SSE)
        │
        ▼
InventoryContextTool.getInventoryContext()
        │
        ▼
InventoryKnowledgeService.getInventoryContext()
  → InventoryKnowledgeProperties.getItems() 전체 반환
  → format(): 마크다운으로 변환
        │
        ▼
도메인 지식 문자열 (마크다운)
  예약/확정/복구 단계, 동시성 제어, 멱등성, Saga 패턴...
        │
        ▼ MCP 응답
        │
        ▼
AI 코딩 도구
  → 도메인 지식을 컨텍스트에 포함하여 코드 생성
        │
        ▼
개발자: 오버셀링 방지, 낙관락, 멱등성이 적용된 재고 로직 수령
```

---

## 지식 데이터 스키마

### 도메인별 표준 스키마 (inventory / payment / settlement / coupon)

```yaml
{domain}:
  items:
    - id: string               # 고유 식별자 (kebab-case)
      category: string         # 도메인별 분류 (lifecycle | concurrency | checklist 등)
      title: string            # 표시 제목 (마크다운 ## 헤더로 사용됨)
      content: string          # 지식 내용 (멀티라인, 마크다운 허용)
      tags: list[string]       # 검색 키워드 (영어 소문자, 하이픈)
```

### 범용 이커머스 정규화 스키마 (commerce / spring-commerce)

```yaml
commerce:
  items:
    - id: string
      category: string         # catalog | pricing | order | inventory | payment | customer | checkout | ...
      title: string
      summary: string          # 한 줄 요약
      business-context: string # 유통 비즈니스 맥락
      invariants: list         # 반드시 지켜야 할 원칙
      workflow: list           # 권장 처리 흐름
      technical-guidance: list # 기술 구현 참고
      failure-scenarios: list  # 장애 및 실패 시나리오
      checklist: list          # 검토 질문
      tags: list[string]
```

> `spring-commerce.yml`은 `spring-guidance`, `avoid-patterns`, `checklist` 필드로 구성되어
> Java/Spring 구현 관점의 지침을 담는다. Spring 세부 구현뿐 아니라 Java 값 객체, null/예외,
> 컬렉션/Stream, 설정, 보안, 마이그레이션 기준도 포함한다.

---

## Transport 모드

### STDIO (Claude Code / npx 연결)

```bash
java -jar context-engine.jar --spring.profiles.active=stdio
```

STDIO 모드에서는 로그가 `logs/context-engine-stdio.log` 파일로만 기록되어 stdout이 깨끗하게 유지된다.
`npx commerce-context-mcp` 실행 시 이 모드로 자동 기동된다.

### SSE / Streamable HTTP (Cursor 직접 연결)

**프로토콜 흐름**:
```
1. Cursor → GET /sse                              (SSE 스트림 연결)
2. Server → event:endpoint
            data:/mcp/message?sessionId={uuid}
3. Cursor → POST /mcp/message?sessionId={uuid}    (JSON-RPC 요청)
4. Server → SSE data: {JSON-RPC 응답}             (열린 스트림으로 응답)
```

**Cursor 직접 연결 설정** (`.cursor/mcp.json`):
```json
{
  "mcpServers": {
    "commerce-context": {
      "url": "http://localhost:8080/sse",
      "type": "sse"
    }
  }
}
```

> `./gradlew bootRun` 으로 서버를 먼저 실행해야 한다.

**검증 결과** (2026-06-02):
- `tools/list` → 31개 도구 정상 반환
- `tools/call get_inventory_checklist` → 재고 체크리스트 정상 반환

---

## 확장 계획

### Core API 분리 계획

현재 서비스는 MCP 응답에 맞춘 Markdown 문자열을 반환한다.
MCP 밖에서도 Java 라이브러리처럼 재사용하려면 `KnowledgeEntry`, `KnowledgeQuery`,
`KnowledgeSearchResult`, `KnowledgeRepository`, `KnowledgeRenderer` 같은 구조화 API가 필요하다.

상세 계획은 `docs/CORE_MODULE_PLAN.md`를 따른다.
일주일 내 배포 목표를 고려해 초기에는 Gradle 멀티 모듈보다 패키지 경계를 먼저 만든다.

### 새 도메인 추가 패턴

```
1. docs/DOMAIN_KNOWLEDGE_REFERENCE.md 에 지식 정리
2. src/main/resources/knowledge/{domain}.yml 작성
3. domain/{domain}/{Domain}KnowledgeProperties.java 추가
4. config/KnowledgeConfig.java 에 @PropertySource + @EnableConfigurationProperties 추가
5. service/{Domain}KnowledgeService.java 작성
6. tool/{Domain}ContextTool.java 에 @Tool 메서드 추가
7. config/McpToolConfig.java 에 toolObjects() 인자 추가
8. docs/CLAUDE.md 도구 목록 업데이트
```

### DB 전환 계획

- `{Domain}KnowledgeService`의 의존성을 `{Domain}KnowledgeProperties` → `{Domain}KnowledgeRepository` (JPA) 로 교체
- Tool Layer, 나머지 계층 변경 없음
- 관리자 API (지식 CRUD) 추가
- 벡터 검색 (pgvector) 도입 시 Service의 `search()` 메서드만 교체

---

## 의존성 그래프

```
{Domain}ContextTool  (×6)
    └── {Domain}KnowledgeService  (×6)
            └── {Domain}KnowledgeProperties  ← @ConfigurationProperties
                    └── {domain}.yml          ← YAML 파일

McpToolConfig
    └── allToolCallbackProvider(
            inventoryTool, paymentTool, settlementTool,
            couponTool, commerceTool, springCommerceTool
        )                                      ← ToolCallbackProvider 단일 빈

KnowledgeConfig
    ├── YamlPropertySourceFactory              ← YAML 파싱
    ├── InventoryKnowledgeProperties
    ├── PaymentKnowledgeProperties
    ├── SettlementKnowledgeProperties
    ├── CouponKnowledgeProperties
    ├── CommerceKnowledgeProperties
    └── SpringCommerceKnowledgeProperties      ← @EnableConfigurationProperties
```
