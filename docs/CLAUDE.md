# Commerce Context MCP — CLAUDE.md

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
├── domain/
│   ├── SimpleKnowledgeItem.java           ← 단순 4개 도메인 공통 아이템 모델 (@Data)
│   ├── inventory/InventoryKnowledgeProperties.java  ← @ConfigurationProperties("inventory")
│   ├── payment/PaymentKnowledgeProperties.java      ← @ConfigurationProperties("payment")
│   ├── settlement/SettlementKnowledgeProperties.java← @ConfigurationProperties("settlement")
│   └── coupon/CouponKnowledgeProperties.java        ← @ConfigurationProperties("coupon")
├── domain/commerce/
│   └── CommerceKnowledgeProperties.java   ← @ConfigurationProperties("commerce"), 정규화 범용 지식
├── domain/springcommerce/
│   └── SpringCommerceKnowledgeProperties.java ← @ConfigurationProperties("spring-commerce"), Spring 구현 지식
├── core/                              ← MCP 밖에서도 쓰는 구조화 지식 API (순수 Java, Spring 의존 없음)
│   ├── KnowledgeEntry.java            ← 정규화 지식 단위 (domain·id·category·title·summary·content·sections·checklist·tags)
│   ├── KnowledgeQuery.java            ← 검색 조건 (keyword·domain·category·limit)
│   ├── KnowledgeSearchResult.java     ← 검색 결과 단위 (entry·matchedFields·score)
│   ├── KnowledgeRepository.java       ← 저장소 인터페이스 (YAML→DB 전환 시 이것만 교체)
│   ├── KnowledgeSearchService.java    ← 검색 서비스 인터페이스
│   └── KnowledgeRenderer.java         ← Markdown 렌더러 인터페이스
├── repository/
│   ├── KnowledgeEntryMapper.java      ← fromSimple(domain, SimpleKnowledgeItem) + from(Commerce/SpringCommerce) (package-private)
│   └── YamlKnowledgeRepository.java   ← KnowledgeRepository 구현체 (6개 YAML, @PostConstruct 1회 캐시 빌드)
├── service/
│   ├── DefaultKnowledgeSearchService.java ← KnowledgeSearchService 구현 (스코어링·필터·limit)
│   └── MarkdownKnowledgeRenderer.java     ← KnowledgeRenderer 구현 (renderRich 단일 경로)
└── tool/
    ├── InventoryContextTool.java          ← @Tool 6개 (재고)
    ├── PaymentContextTool.java            ← @Tool 8개 (결제)
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

**YAML 스키마 (4종)**:

① inventory/payment/settlement/coupon — 구조화 도메인 스키마
```yaml
inventory:
  items:
    - id: unique-id
      category: lifecycle | concurrency | idempotency | consistency | checklist
      title: "표시 제목"
      summary: "한 줄 요약"
      guidance:
        - "구현 가이드 항목"
      avoid-patterns:
        - "피해야 할 패턴"
      checklist:
        - "체크 항목 (checklist 카테고리 필수, 그 외는 선택)"
      tags: [tag1, tag2]
```

② commerce — 범용 이커머스 정규화 스키마
```yaml
commerce:
  items:
    - id: unique-id
      category: catalog | pricing | order | inventory | payment | fulfillment | promotion | ...
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

③ spring-commerce — Java Spring 구현 지식 스키마
```yaml
spring-commerce:
  items:
    - id: unique-id
      summary: "핵심 요약"
      business-context: "이커머스 맥락"
      spring-guidance: ["Spring 구현 가이드"]
      avoid-patterns: ["피해야 할 패턴"]
      checklist: ["검토 질문"]
      tags: [tag1, tag2]
```

새 지식 항목 추가는 해당 YAML만 편집하면 된다. 코드 변경 불필요.

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

**결제 도메인** (8개)

| 도구명 | 설명 |
|--------|------|
| `get_payment_state_machine_guide` | 결제 상태 머신과 전이 가드 (PENDING→PAID/UNCERTAIN/CANCELLED) |
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

**전체 통합 검색** (1개)

| 도구명 | 설명 |
|--------|------|
| `search_all_knowledge` | 도메인 구분 없이 전체 지식 통합 키워드 검색. 결과는 score 내림차순 |

---

## 개발 규칙

### 새 도메인 모듈 추가 순서

```
1. docs/DOMAIN_KNOWLEDGE_REFERENCE.md 에 지식 정리
2. src/main/resources/knowledge/{domain}.yml 작성
3. domain/{domain}/{Domain}KnowledgeProperties.java 추가
   - 단순 도메인: List<SimpleKnowledgeItem> items; 만 선언 (inner Item 클래스 불필요)
   - 리치 도메인: 별도 Item inner class + 필드 정의
4. config/KnowledgeConfig.java 에 @PropertySource + @EnableConfigurationProperties 추가
5. repository/KnowledgeEntryMapper.java
   - 단순 도메인: 추가 불필요 (fromSimple(domain, item) 범용 메서드 재사용)
   - 리치 도메인: from({Domain}KnowledgeProperties.Item) 매퍼 추가
6. repository/YamlKnowledgeRepository.java 에 필드·map{Domain}() 메서드 추가
7. tool/{Domain}ContextTool.java 작성 (KnowledgeSearchService + KnowledgeRenderer 주입)
8. config/McpToolConfig.java 에 allToolCallbackProvider() 인자 추가
   + src/main/resources/application.yml 의 mcp.server.annotation-scanner.tool-beans 목록도 함께 동기화
9. docs/CLAUDE.md 의 도구 목록 업데이트
```

> 서비스 클래스를 별도로 만들 필요 없음. 검색·렌더링은 공통 인프라(`DefaultKnowledgeSearchService`, `MarkdownKnowledgeRenderer`)가 처리한다.

### Core 모듈 고도화 계획

패키지 경계(core / repository / service / tool)가 완성됐다.
향후 필요 시 Gradle 멀티 모듈로 분리하여 `context-engine-core`를 독립 Java 라이브러리로 배포할 수 있다.

### 외부 사용자 사용성

외부 사용자는 npm에 공개된 `commerce-context-mcp`를 `npx`로 실행한다.
`--help`, `doctor`, Claude/Cursor 설정 복붙 같은 외부 사용자 진입점을 우선 유지한다.
설치와 릴리즈 운영 절차는 `docs/OPERATIONS.md`를 따른다.

### 코드 스타일

- Lombok 사용 (`@Data`, `@RequiredArgsConstructor`)
- `@Tool` description은 한국어로, 어떤 상황에 호출해야 하는지 명시
- `@ToolParam` description은 파라미터 예시까지 포함
- Tool 클래스는 `KnowledgeSearchService` + `KnowledgeRenderer`에만 의존. 포맷 로직 직접 구현 금지

### 금지 사항

- SnakeYAML raw Map 캐스팅 금지 (`@ConfigurationProperties`로 대체)
- `@PostConstruct` 내 YAML 직접 파싱 금지 — Spring 바인딩 사용. 단, `YamlKnowledgeRepository`의 엔트리 캐시 빌드처럼 바인딩 결과를 1회 변환하는 용도는 허용
- 도구 description에 구현 세부사항 기술 금지 (호출 트리거 조건만 작성)

---

## 테스트

```bash
./gradlew test        # 전체 단위 + 통합 테스트
./gradlew validateKnowledge  # YAML 지식 스키마와 품질 규칙 검증
```

**테스트 범위**:
- `KnowledgeSchemaValidationTest` — 전체 YAML 지식 ID 중복, 허용 category, 필수 필드, 태그 검증
- `YamlKnowledgeRepositoryTest` — 8개: 전체 항목 수(≥66), 도메인 필터, sections 구조, 필수 필드 검증
- `DefaultKnowledgeSearchServiceTest` — 14개: 키워드 검색, 스코어 정렬, 도메인 필터, limit, matchedFields, 다중 단어·띄어쓰기 무관 검색
- `MarkdownKnowledgeRendererTest` — 12개: structured/rich/spring-commerce 렌더링, 섹션 헤더, numbered/bullet/checkbox 리스트
- `InventoryContextToolTest` — 8개: 33개 도구 등록 확인, 재고 도구 응답 내용 검증
- `PaymentContextToolTest` — 9개: 결제 도구 등록 확인, state-machine 포함 각 도구 응답 검증
- `SettlementContextToolTest` — 6개: 각 정산 도구 응답 내용 검증
- `CouponContextToolTest` — 6개: 각 쿠폰 도구 응답 내용 검증
- `CommerceContextToolTest` — 3개: 범용 이커머스 도구 응답 검증
- `SpringCommerceContextToolTest` — 3개: Java Spring MCP 도구 응답 검증
- `UnifiedSearchToolTest` — 5개: 전체 도메인 통합 검색, 빈 키워드, 결과 없음 처리
- `ContextEngineApplicationTests` — 1개: 컨텍스트 로드 확인

---

## 리포 구조 (배포 방식)

| 리포 | 공개 여부 | 용도 |
|------|----------|------|
| `dakcoh/commerce-context-mcp` | **public** | 소스 코드 + JAR 릴리즈 + npm 패키지 |

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
./gradlew bootJar "-Pversion=<new-version>"

# 2. dakcoh/commerce-context-mcp 리포에 Release + JAR 수동 업로드

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
- npm package: `commerce-context-mcp@<version>`
- GitHub Release: `v<version>`
- 사용자 실행: `npx -y commerce-context-mcp doctor`

---

## 빌드 & 개발 실행

```bash
# 빌드
./gradlew build

# Streamable HTTP 모드 실행 (Cursor 연결)
./gradlew bootRun

# STDIO 모드 실행 (Claude Code 연결)
java -jar build/libs/<jar-file> --spring.profiles.active=stdio
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
- 웹 서버(Tomcat) 미기동 (`web-application-type=none`) — 콜드 스타트·메모리 절감, 포트 점유 없음
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
| 6단계 | Core API 분리 + 통합 검색 고도화 | ✅ 완료 (core 패키지 경계, 스코어링 검색, 통합 검색 Tool) |
| 7단계 | npm 공개 배포 | ✅ 완료 (5-1단계와 함께 선행 완료) |
| 8단계 | Simple 도메인 스키마 고도화 + 심층 지식 추가 | ✅ 완료 (구조화 스키마 + 가용재고 조회·결제 상태머신·Redis보상 패턴 신규 항목) |
| 8-1단계 | 코드 품질 개선 | ✅ 완료 (ISP 적용·renderSimple 제거·SimpleKnowledgeItem DRY·@PostConstruct 캐시·state-machine tool 추가) |
| 9단계 | Gradle 멀티 모듈 분리 (context-engine-core 독립 배포) | 🔲 필요 시 |
