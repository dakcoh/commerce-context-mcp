# Commerce Context Engine — CLAUDE.md

> AI 어시스턴트가 이 프로젝트를 작업할 때 반드시 읽어야 하는 컨텍스트 문서.

---

## 프로젝트 한 줄 요약

**Spring AI MCP Server** 기반의 Java Spring 이커머스 웹 개발 지식 주입 서버.
Claude Code / Cursor 같은 AI 코딩 도구가 이커머스 백엔드를 개발할 때,
유통 도메인 함정(오버셀링, 멱등성 누락, 분산 트랜잭션 미처리 등)과
Spring 구현 함정(긴 트랜잭션, JPA N+1, 중복 배치, Redis 원장 오용 등)을 자동으로 경고한다.

---

## 기술 스택

| 항목 | 값 |
|------|----|
| Language | Java 17+ |
| Framework | Spring Boot 3.5.x |
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
│   └── InventoryKnowledgeProperties.java  ← @ConfigurationProperties("inventory")
├── domain/payment/
│   └── PaymentKnowledgeProperties.java    ← @ConfigurationProperties("payment")
├── domain/settlement/
│   └── SettlementKnowledgeProperties.java ← @ConfigurationProperties("settlement")
├── domain/coupon/
│   └── CouponKnowledgeProperties.java     ← @ConfigurationProperties("coupon")
├── domain/commerce/
│   └── CommerceKnowledgeProperties.java   ← @ConfigurationProperties("commerce"), 정규화 범용 지식
├── domain/springcommerce/
│   └── SpringCommerceKnowledgeProperties.java ← @ConfigurationProperties("spring-commerce"), Spring 구현 지식
├── core/                              ← 계획: MCP 밖에서도 쓰는 구조화 지식 API
│   ├── KnowledgeEntry.java            ← 계획
│   ├── KnowledgeQuery.java            ← 계획
│   ├── KnowledgeSearchResult.java     ← 계획
│   └── KnowledgeSearchService.java    ← 계획
├── service/
│   ├── KnowledgeSearchSupport.java        ← 검색 유틸 (normalize·contains·safeList 등)
│   ├── InventoryKnowledgeService.java     ← 재고 지식 쿼리 로직
│   ├── PaymentKnowledgeService.java       ← 결제 지식 쿼리 로직
│   ├── SettlementKnowledgeService.java    ← 정산 지식 쿼리 로직
│   ├── CouponKnowledgeService.java        ← 쿠폰 지식 쿼리 로직
│   ├── CommerceKnowledgeService.java      ← 범용 이커머스 지식 검색·포맷
│   └── SpringCommerceKnowledgeService.java ← Java Spring 구현 지식 검색·포맷
└── tool/
    ├── InventoryContextTool.java          ← @Tool 6개 (재고)
    ├── PaymentContextTool.java            ← @Tool 7개 (결제)
    ├── SettlementContextTool.java         ← @Tool 6개 (정산)
    ├── CouponContextTool.java             ← @Tool 6개 (쿠폰/프로모션)
    ├── CommerceContextTool.java           ← @Tool 3개 (범용 이커머스)
    └── SpringCommerceContextTool.java     ← @Tool 3개 (Java Spring 구현)
```

---

## 지식 데이터 위치

```
src/main/resources/knowledge/inventory.yml    ← 재고 도메인 지식
src/main/resources/knowledge/payment.yml      ← 결제 도메인 지식
src/main/resources/knowledge/settlement.yml   ← 정산 도메인 지식
src/main/resources/knowledge/coupon.yml       ← 쿠폰/프로모션 도메인 지식
src/main/resources/knowledge/commerce.yml     ← 범용 이커머스 정규화 지식 20개
src/main/resources/knowledge/spring-commerce.yml ← Java Spring 이커머스 구현 지식 20개
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

`commerce.yml`은 범용 지식을 DB 전환 전에 검증하기 위한 정규화 스키마를 사용한다.

```yaml
commerce:
  items:
    - id: unique-id
      category: catalog | pricing | order | inventory | payment | fulfillment | promotion | distribution | settlement | operations | customer | checkout | search | claim | security | loyalty | membership | review | subscription
      title: "표시 제목"
      summary: "핵심 요약"
      business-context: "이커머스 유통 맥락"
      invariants: ["반드시 지켜야 할 원칙"]
      workflow: ["권장 처리 흐름"]
      technical-guidance: ["기술 구현 참고"]
      failure-scenarios: ["장애 및 실패 시나리오"]
      checklist: ["검토 질문"]
      tags: [tag1, tag2]
```

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

**정산 도메인** (6개)

| 도구명 | 설명 |
|--------|------|
| `get_settlement_timing` | 정산 시점과 기준일 처리 가이드 |
| `get_settlement_deduction` | 정산 공제와 음수 정산 처리 |
| `get_settlement_cycle` | 정산 배치 멱등성과 실패 복구 |
| `get_settlement_integrity` | 정산 정합성과 타임존 검증 |
| `get_settlement_checklist` | 정산 AI 구현 실수 체크리스트 |
| `search_settlement_knowledge` | 정산 도메인 키워드 검색 |

**쿠폰/프로모션 도메인** (6개)

| 도구명 | 설명 |
|--------|------|
| `get_coupon_validation_guide` | 쿠폰 유효성 검증 가이드 |
| `get_coupon_discount_calculation` | 할인 캡과 부분 취소 역산 |
| `get_coupon_issuance_guide` | 선착순 쿠폰 발급 동시성 처리 |
| `get_promotion_rules_guide` | 프로모션 규칙 엔진 설계 |
| `get_coupon_checklist` | 쿠폰 AI 구현 실수 체크리스트 |
| `search_coupon_knowledge` | 쿠폰/프로모션 키워드 검색 |

**범용 이커머스 지식** (3개)

| 도구명 | 설명 |
|--------|------|
| `get_commerce_foundation_context` | 범용 이커머스 정규화 지식 20개 전체 조회 |
| `get_commerce_foundation_checklist` | 범용 설계 검토 체크리스트 |
| `search_commerce_knowledge` | 여러 이커머스 도메인의 통합 키워드 검색 |

**Java Spring 이커머스 구현 지식** (3개)

| 도구명 | 설명 |
|--------|------|
| `get_spring_commerce_implementation_context` | Java Spring 이커머스 구현 지식 20개 전체 조회 |
| `get_spring_commerce_checklist` | Spring 백엔드 구현 체크리스트 |
| `search_spring_commerce_knowledge` | Spring 구현 지식 키워드 검색 |

---

## 개발 규칙

### 새 도메인 모듈 추가 순서

```
1. docs/DOMAIN_KNOWLEDGE_REFERENCE.md 에 지식 정리
2. src/main/resources/knowledge/{domain}.yml 작성
3. domain/{domain}/{Domain}KnowledgeProperties.java 추가
4. config/KnowledgeConfig.java 에 @PropertySource + @EnableConfigurationProperties 추가
5. service/{Domain}KnowledgeService.java 작성
6. tool/{Domain}ContextTool.java 에 @Tool 메서드 추가
7. config/McpToolConfig.java 에 ToolCallbackProvider 빈 추가
8. docs/CLAUDE.md 의 도구 목록 업데이트
```

### Core 모듈 고도화 계획

MCP 서버 밖에서 Java 라이브러리처럼 재사용할 수 있도록 `docs/CORE_MODULE_PLAN.md`를 기준으로 core API를 분리한다.
초기에는 Gradle 멀티 모듈보다 패키지 경계를 먼저 만들고, 안정화 후 모듈 분리를 검토한다.

### 외부 사용자 사용성

외부 사용자는 npm에 공개된 `commerce-context-mcp`를 `npx`로 실행한다.
`--help`, `doctor`, Claude/Cursor 설정 복붙 같은 외부 사용자 진입점을 우선 유지한다.
설치와 릴리즈 운영 절차는 `docs/OPERATIONS.md`를 따른다.

### 코드 스타일

- Lombok 사용 (`@Data`, `@RequiredArgsConstructor`)
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
./gradlew test        # 전체 단위 + 통합 테스트
./gradlew validateKnowledge  # YAML 지식 스키마와 품질 규칙 검증
```

**테스트 범위**:
- `KnowledgeSchemaValidationTest` — 전체 YAML 지식 ID 중복, 허용 category, 필수 필드, 태그 검증
- `InventoryKnowledgeServiceTest` — 카테고리 필터, 키워드 검색, 미등록/빈 키워드 처리
- `InventoryContextToolTest` — 9개: 31개 도구 등록 확인, 재고 도구 응답 내용 검증
- `PaymentKnowledgeServiceTest` — 8개: 웹훅/중복/망취소/환불/멱등성/체크리스트/검색
- `PaymentContextToolTest` — 7개: 각 결제 도구 응답 내용 검증
- `SettlementKnowledgeServiceTest` — 7개: 시점/공제/배치/정합성/체크리스트/검색
- `SettlementContextToolTest` — 6개: 각 정산 도구 응답 내용 검증
- `CouponKnowledgeServiceTest` — 7개: 검증/계산/발급/프로모션/체크리스트/검색
- `CouponContextToolTest` — 6개: 각 쿠폰 도구 응답 내용 검증
- `CommerceKnowledgeServiceTest` — 정규화 필드, 통합 검색, 추적 메타데이터 검증
- `CommerceContextToolTest` — 3개: 범용 이커머스 도구 응답 검증
- `SpringCommerceKnowledgeServiceTest` — Spring/Java 지식 정규화, 검색 검증
- `SpringCommerceContextToolTest` — 3개: Java Spring MCP 도구 응답 검증
- `ContextEngineApplicationTests` — 1개: 컨텍스트 로드 확인

---

## 리포 구조 (배포 방식)

| 리포 | 공개 여부 | 용도 |
|------|----------|------|
| `dakcoh/context-engine` | **public** | 소스 코드 + JAR 릴리즈 + npm 패키지 |

## npm 패키지 구조

```
npm/
├── package.json         ← 패키지명: commerce-context-mcp
└── bin/
    └── index.js         ← Java 체크 → JAR 다운로드(public 리포) → STDIO 실행
```

**릴리즈 방법** (상세 절차는 docs/OPERATIONS.md 참고):
```bash
# 1. JAR 빌드
./gradlew bootJar "-Pversion=0.0.2"

# 2. dakcoh/context-engine 리포에 Release + JAR 수동 업로드

# 3. GitHub Release를 Publish하면 GitHub Actions가 npm 자동 배포
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

**현재 공개 버전**:
- npm package: `commerce-context-mcp@0.0.1`
- GitHub Release: `v0.0.1`
- 사용자 실행: `npx commerce-context-mcp doctor`

---

## 빌드 & 개발 실행

```bash
# 빌드
./gradlew build

# Streamable HTTP 모드 실행 (Cursor 연결)
./gradlew bootRun

# STDIO 모드 실행 (Claude Code 연결)
java -jar build/libs/context-engine-0.0.1-SNAPSHOT.jar --spring.profiles.active=stdio
```

## 사용자 MCP 연결 설정

공개 사용자는 로컬 JAR 경로 대신 `npx` 설정을 사용한다.

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
| 7단계 | npm 공개 배포 | ✅ 완료 (5-1단계와 함께 선행 완료) |
