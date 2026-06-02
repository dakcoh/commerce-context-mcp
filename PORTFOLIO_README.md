# Commerce Context Engine

> AI 코딩 도구가 이커머스 백엔드를 개발할 때 빠뜨리는 도메인 함정을 자동으로 주입하는 **MCP 도메인 지식 서버**

---

## 왜 이걸 만들었나

AI 코딩 도구(Claude Code, Cursor)는 코드를 잘 짠다. 하지만 이커머스 백엔드의 실전 함정은 모른다.

```
개발자: "재고 차감 로직 만들어줘"

일반 AI 결과:              Commerce Context Engine 결과:
stock = stock - 1   →     재고 예약 → 확정 → 복구 단계 분리
                           오버셀링 방지 (동시 요청 제어)
                           낙관락 / 비관락 / 분산락 선택 기준
                           주문-결제-재고 정합성 (Saga 패턴)
                           멱등성 (중복 요청 방어)
```

**이 프로젝트의 해자(Moat)**: mem0, Zep 같은 범용 메모리 도구는 "당신 프로젝트를 기억"할 뿐,
"이커머스 백엔드를 어떻게 짜야 하는가"는 모른다. 이커머스 도메인 지식 자체가 차별점이다.

---

## 어떻게 동작하나

```
개발자 → AI 도구 → (MCP 프로토콜) → Commerce Context Engine → 도메인 지식 반환
                                                                        ↓
                                          AI가 도메인 지식을 컨텍스트에 포함해 코드 생성
```

**사용자 세팅 — 단 두 줄로 끝:**

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

Java가 있으면 이게 전부다. npx가 JAR를 자동으로 내려받고 실행한다.

**연결 방식:**
- **Claude Code**: `.claude/settings.json` — STDIO 자동 연결
- **Cursor**: `.cursor/mcp.json` — STDIO 자동 연결 (HTTP 없이 동일 설정 사용 가능)

---

## 현재 제공하는 도메인 지식 (MCP 도구 24개)

### 재고 도메인 (6개)

| 도구 | 해결하는 문제 |
|------|-------------|
| `get_inventory_context` | "재고 차감" 요청 시 예약/확정/복구 단계 전체 컨텍스트 제공 |
| `get_concurrency_strategy` | 낙관락/비관락/분산락 중 어떤 것을 언제 써야 하는지 |
| `get_saga_pattern` | 주문-결제-재고 분산 트랜잭션 실패 시 보상 흐름 |
| `get_idempotency_guide` | 네트워크 재시도로 인한 이중 차감 방어 |
| `get_inventory_checklist` | 구현 완료 후 AI가 빠뜨린 항목 검토 |
| `search_inventory_knowledge` | 키워드 기반 재고 지식 검색 |

### 결제 도메인 (7개)

| 도구 | 해결하는 문제 |
|------|-------------|
| `get_payment_webhook_guide` | PG사 웹훅 처리 즉시 200, 비동기 처리, 서명 검증 |
| `get_duplicate_payment_guard` | 연속 클릭·네트워크 재시도로 인한 이중 결제 3중 방어 |
| `get_network_cancellation_guide` | 결제 요청 후 응답 없음 — 망취소 상태 관리 |
| `get_partial_refund_guide` | 쿠폰·포인트 병행 사용 주문의 부분 환불 금액 역산 |
| `get_payment_idempotency_guide` | 결제 API Idempotency-Key 설계 |
| `get_payment_checklist` | 결제 구현 완료 후 검토 체크리스트 |
| `search_payment_knowledge` | 키워드 기반 결제 지식 검색 |

### 정산 도메인 (6개)

| 도구 | 해결하는 문제 |
|------|-------------|
| `get_settlement_timing` | 구매 확정/배송 완료/결제 완료 기준 정산 시점 설계 |
| `get_settlement_deduction` | 수수료·반품·프로모션 분담금 공제, 음수 정산 처리 |
| `get_settlement_cycle` | 정산 배치 멱등성, 대용량 페이징, 실패 복구 |
| `get_settlement_integrity` | 금액·건수 정합성 검증, 타임존(UTC/KST) 주의사항 |
| `get_settlement_checklist` | 정산 구현 완료 후 검토 체크리스트 |
| `search_settlement_knowledge` | 키워드 기반 정산 지식 검색 |

### 쿠폰/프로모션 도메인 (6개 - 1개 검색 포함)

| 도구 | 해결하는 문제 |
|------|-------------|
| `get_coupon_validation_guide` | 유효 기간·대상 조건·중복 사용·동시성 5가지 검증 |
| `get_coupon_discount_calculation` | 정률/정액 할인 캡, 복수 쿠폰 순서, 부분 취소 역산 |
| `get_coupon_issuance_guide` | 선착순 발급 오버이슈 방지 — Redis/DB락/큐 전략 |
| `get_promotion_rules_guide` | if-else 한계 극복, DB 기반 규칙 엔진 설계 |
| `get_coupon_checklist` | 쿠폰/프로모션 구현 완료 후 검토 체크리스트 |
| `search_coupon_knowledge` | 키워드 기반 쿠폰/프로모션 지식 검색 |

---

## 기술 구조

```
┌─────────────────────────────────────────────────────┐
│  AI 코딩 도구 (Claude Code / Cursor)                 │
└──────────────────┬──────────────────────────────────┘
                   │ MCP Protocol
                   ▼
┌─────────────────────────────────────────────────────┐
│  Tool Layer  — @Tool 24개                           │
│  InventoryContextTool  / PaymentContextTool          │
│  SettlementContextTool / CouponContextTool           │
├─────────────────────────────────────────────────────┤
│  Service Layer — 카테고리 필터링 · 키워드 검색        │
├─────────────────────────────────────────────────────┤
│  Knowledge Layer — @ConfigurationProperties          │
│  inventory.yml / payment.yml                         │
│  settlement.yml / coupon.yml                         │
└─────────────────────────────────────────────────────┘
```

**지식 추가 방법**: YAML 파일만 편집하면 끝. 코드 변경 불필요.

---

## 기술 스택 & 설계 포인트

| 항목 | 선택 | 이유 |
|------|------|------|
| Spring AI MCP Server | `spring-ai-starter-mcp-server-webmvc` | STDIO + HTTP 모두 지원 |
| 지식 저장 | YAML + `@ConfigurationProperties` | 재배포 없이 지식 수정 가능, Git 버전 관리 |
| 도구 등록 | `MethodToolCallbackProvider` | 어노테이션 기반, 타입 안전 |
| STDIO 로그 격리 | `logback-stdio.xml` + 별도 프로파일 | stdout 오염 없이 MCP 통신 보장 |
| Java | 21 (Virtual Threads 준비) | 미래 고성능 확장 고려 |

---

## 실행 방법

```bash
# 빌드
./gradlew build

# HTTP 모드 (Cursor 연결)
./gradlew bootRun

# STDIO 모드 (Claude Code 연결)
java -jar build/libs/context-engine-0.0.1-SNAPSHOT.jar \
     --spring.profiles.active=stdio
```

**Claude Code / Cursor** (공통 설정):
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

설정 파일만 추가하면 끝. Java 설치 외에 사전 작업 없음.

---

## 로드맵

| 단계 | 내용 | 상태 |
|------|------|------|
| 1단계 | MCP 서버 골격 | ✅ 완료 |
| 2단계 | 재고 도메인 | ✅ 완료 |
| 3단계 | Claude Code 연동 | ✅ 완료 |
| 4단계 | 결제 도메인 | ✅ 완료 |
| 5단계 | 정산 + 쿠폰 도메인 | ✅ 완료 |
| 6단계 | 지식 저장소 DB 전환 + 관리자 API | 🔲 예정 |
| 7단계 | Beta 공개 | 🔲 예정 |

---

## 테스트

```
46개 테스트 / 46개 통과 (0 failures)

ContextEngineApplicationTests      1개  — 컨텍스트 로드
InventoryKnowledgeServiceTest       7개  — 재고 서비스
PaymentKnowledgeServiceTest         8개  — 결제 서비스
SettlementKnowledgeServiceTest      7개  — 정산 서비스
CouponKnowledgeServiceTest          7개  — 쿠폰 서비스
InventoryContextToolTest            9개  — MCP 도구 등록 + 응답 검증
PaymentContextToolTest              7개  — 결제 도구 응답 검증
```
