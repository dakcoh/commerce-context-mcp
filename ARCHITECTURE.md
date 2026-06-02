# Commerce Context Engine — Architecture

> 상세 아키텍처 문서. 시스템 구조, 계층 책임, 데이터 흐름을 기술한다.

---

## 전체 구조

```
┌─────────────────────────────────────────────────────┐
│                  AI 코딩 도구                         │
│  ┌──────────────────┐   ┌──────────────────────────┐ │
│  │   Claude Code    │   │         Cursor           │ │
│  │   (STDIO 모드)   │   │  (Streamable HTTP 모드)  │ │
│  └────────┬─────────┘   └─────────────┬────────────┘ │
└───────────┼─────────────────────────────┼────────────┘
            │       MCP Protocol          │
            └──────────────┬──────────────┘
                           ▼
┌──────────────────────────────────────────────────────┐
│              Commerce Context Engine                 │
│              (Spring Boot 3.5 + Spring AI MCP)       │
│                                                      │
│  ┌─────────────────────────────────────────────────┐ │
│  │                  Tool Layer                     │ │
│  │  InventoryContextTool (@Tool × 6)               │ │
│  │  • get_inventory_context                        │ │
│  │  • get_concurrency_strategy                     │ │
│  │  • get_saga_pattern                             │ │
│  │  • get_idempotency_guide                        │ │
│  │  • get_inventory_checklist                      │ │
│  │  • search_inventory_knowledge(keyword)          │ │
│  └────────────────────┬────────────────────────────┘ │
│                       │                              │
│  ┌────────────────────▼────────────────────────────┐ │
│  │               Service Layer                     │ │
│  │  InventoryKnowledgeService                      │ │
│  │  • category 필터링                               │ │
│  │  • 키워드 검색 (title + content + tags)          │ │
│  │  • 마크다운 포맷 변환                             │ │
│  └────────────────────┬────────────────────────────┘ │
│                       │                              │
│  ┌────────────────────▼────────────────────────────┐ │
│  │             Knowledge Layer                     │ │
│  │  InventoryKnowledgeProperties                   │ │
│  │  (@ConfigurationProperties)                     │ │
│  │                                                 │ │
│  │  ← knowledge/inventory.yml                      │ │
│  └─────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────┘
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
        ▼ MCP Protocol (STDIO or Streamable HTTP)
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

```yaml
inventory:
  items:
    - id: string               # 고유 식별자 (kebab-case)
      category: string         # lifecycle | concurrency | idempotency | consistency | checklist
      title: string            # 표시 제목 (마크다운 ## 헤더로 사용됨)
      content: string          # 지식 내용 (멀티라인, 마크다운 허용)
      tags: list[string]       # 검색 키워드 (영어 소문자, 하이픈)
```

**category 분류 기준**:

| category | 의미 |
|----------|------|
| `lifecycle` | 재고 상태 흐름 (예약→확정→복구) |
| `concurrency` | 동시성 제어 전략 |
| `idempotency` | 중복 요청 방어 |
| `consistency` | 분산 정합성 (Saga, Outbox) |
| `checklist` | 구현 완료 후 검토 체크리스트 |

---

## Transport 모드

### Streamable HTTP (기본, Cursor 연결)

```
POST /mcp/message
GET  /mcp/sse
```

`application.yml`:
```yaml
spring:
  ai:
    mcp:
      server:
        type: SYNC
```

### STDIO (Claude Code 연결)

```bash
java -jar context-engine.jar --spring.profiles.active=stdio
```

STDIO 모드에서는 로그가 `logs/context-engine-stdio.log` 파일로만 기록되어 stdout이 깨끗하게 유지된다.

### Streamable HTTP / SSE (Cursor 연결)

**프로토콜 흐름**:
```
1. Cursor → GET /sse          (SSE 스트림 연결)
2. Server → event:endpoint
            data:/mcp/message?sessionId={uuid}
3. Cursor → POST /mcp/message?sessionId={uuid}  (JSON-RPC 요청)
4. Server → SSE data: {JSON-RPC 응답}          (열린 스트림으로 응답)
```

**Cursor `mcp.json` 설정**:
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

> 주의: Cursor는 서버가 미리 실행 중이어야 연결 가능. `./gradlew bootRun` 또는 JAR 실행 후 Cursor 재시작.

**검증 결과** (2026-06-02):
- `tools/list` → 25개 도구 정상 반환
- `tools/call get_inventory_checklist` → 재고 체크리스트 정상 반환

---

## 확장 계획

### 4단계 — 주문/결제 도메인 추가

```
resources/knowledge/payment.yml
domain/payment/PaymentKnowledgeProperties.java
service/PaymentKnowledgeService.java
tool/PaymentContextTool.java
config/McpToolConfig.java  ← ToolCallbackProvider 빈 추가
```

### 5단계 — DB 전환

- `InventoryKnowledgeService`의 의존성을 `InventoryKnowledgeProperties` → `InventoryKnowledgeRepository` (JPA) 로 교체
- Tool Layer, 나머지 계층 변경 없음
- 관리자 API (지식 CRUD) 추가
- 벡터 검색 (pgvector) 도입 시 Service의 `search()` 메서드만 교체

---

## 의존성 그래프

```
InventoryContextTool
    └── InventoryKnowledgeService
            └── InventoryKnowledgeProperties   ← @ConfigurationProperties
                    └── inventory.yml           ← YAML 파일

McpToolConfig
    └── InventoryContextTool                   ← ToolCallbackProvider 등록

KnowledgeConfig
    ├── YamlPropertySourceFactory              ← YAML 파싱
    └── InventoryKnowledgeProperties           ← @EnableConfigurationProperties
```
