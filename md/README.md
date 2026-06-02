# Commerce Context Engine

> Spring 기반 이커머스 백엔드를 개발할 때, AI 코딩 도구가 습관적으로 빠뜨리는 도메인 함정을
> 필요한 순간에 자동으로 주입하는 **MCP 도메인 지식 서버**

---

## 프로젝트 개요

### 핵심 문제

AI 코딩 도구(Claude Code, Cursor)는 코드를 잘 짠다. 하지만 두 가지를 모른다.

1. **프로젝트 현황** — 어떤 스택을 쓰는지, 어디까지 개발됐는지
2. **도메인 지식** — 이커머스 백엔드를 "제대로" 만들려면 무엇을 조심해야 하는지

1번은 이미 `CLAUDE.md`, `AGENTS.md` 같은 무료 기능이 해결하고 있다.
**진짜 비어 있는 것은 2번이다.**

```
개발자: "재고 차감 로직 만들어줘"

일반 AI 결과:          Commerce Context Engine 결과:
stock = stock - 1  →   재고 예약 / 확정 / 복구 단계 분리
                        오버셀링 방지 (동시 요청 제어)
                        낙관락 / 비관락 / 분산락 선택 기준
                        주문-결제-재고 정합성 (Saga 패턴)
                        멱등성 (중복 요청 방어)
```

### 제품 정의

**전달 수단**: MCP 서버 (Spring AI MCP Server)
- Claude Code → STDIO 연결
- Cursor → Streamable HTTP 연결
- 사용자는 패키지 설치 없음. MCP 설정 파일에 서버 주소 한 줄 추가로 끝.

**차별점**: 범용 메모리 도구(mem0, Zep 등)는 "당신 프로젝트를 기억"할 뿐,
"이커머스 백엔드를 어떻게 짜야 하는가"는 모른다. 이 지식이 해자(Moat).

---

## 시스템 아키텍처

```
┌─────────────────────────────────────────┐
│           AI 코딩 도구                   │
│  ┌──────────────┐   ┌─────────────────┐ │
│  │  Claude Code │   │     Cursor      │ │
│  │  (STDIO)     │   │ (Streamable HTTP│ │
│  └──────┬───────┘   └────────┬────────┘ │
└─────────┼────────────────────┼──────────┘
          │    MCP Protocol    │
          └────────┬───────────┘
                   ▼
┌──────────────────────────────────────────────────┐
│         Commerce Context Engine                  │
│         (Spring Boot 3.4 + Spring AI MCP)        │
│                                                  │
│  ┌──────────────┐  ┌────────────────┐  ┌───────┐ │
│  │ MCP Tool     │→ │Knowledge Layer │→ │도메인 │ │
│  │ Layer        │  │지식 검색·선별  │  │모듈   │ │
│  │ (@Tool)      │  │컨텍스트 압축   │  │       │ │
│  └──────────────┘  └────────────────┘  │재고   │ │
│                                        │주문   │ │
│                                        │결제   │ │
│                                        │정산   │ │
│                                        └───────┘ │
└──────────────────────────────────────────────────┘
```

### 핵심 동작 흐름

```
1. 개발자가 AI에게 "재고 차감 로직 만들어줘" 입력
2. AI 코딩 도구가 MCP로 서버에 도구 호출 요청
3. Commerce Context Engine이 "재고" 키워드 인식
4. 재고 모듈에서 관련 지식만 선별·압축
5. AI가 도메인 지식이 포함된 컨텍스트로 코드 생성
```

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.x |
| MCP | Spring AI MCP Server (spring-ai-starter-mcp-server-webmvc) |
| Transport | STDIO (Claude Code) / Streamable HTTP (Cursor) |
| 지식 저장 (MVP) | YAML 파일 |
| 지식 저장 (확장) | PostgreSQL |
| Build | Gradle (Kotlin DSL) |

---

## 프로젝트 구조

```
context-engine/
├── README.md                          ← 이 파일
├── PORTFOLIO_README.md                ← 포트폴리오용 소개
├── ARCHITECTURE.md                    ← 상세 아키텍처 (추후 작성)
├── build.gradle.kts
├── settings.gradle.kts
└── src/
    └── main/
        ├── java/com/commerce/contextengine/
        │   ├── ContextEngineApplication.java
        │   ├── tool/
        │   │   └── InventoryContextTool.java    ← @Tool 메서드 (MCP 노출)
        │   ├── service/
        │   │   └── InventoryKnowledgeService.java
        │   └── domain/
        │       └── inventory/
        │           ├── InventoryKnowledge.java
        │           └── InventoryKnowledgeItem.java
        └── resources/
            ├── application.yml
            └── knowledge/
                └── inventory.yml               ← 재고 도메인 지식 데이터 (MVP)
```

---

## MVP 범위

### 1차 MVP — 재고 도메인 모듈

이커머스에서 사고가 가장 잦고, "일반 AI vs 도메인 지식" 차이가 가장 극명한 영역.

**포함 지식 항목**
- 재고 예약 / 확정 / 복구 단계 분리
- 오버셀링 방지 전략
- 동시성 제어 (낙관락 / 비관락 / 분산락 선택 기준)
- 멱등성 (중복 요청 방어)
- 주문-결제-재고 정합성 (보상 트랜잭션 / Saga 패턴)
- 재고 정산 시점 처리

**노출 MCP 도구 (Tool)**
- `get_inventory_context` — 재고 전체 도메인 컨텍스트 반환
- `get_concurrency_strategy` — 동시성 제어 전략 가이드 반환
- `get_saga_pattern` — Saga 패턴 구현 가이드 반환
- `get_idempotency_guide` — 멱등성 키 설계 가이드 반환
- `get_inventory_checklist` — AI 구현 실수 체크리스트 반환
- `search_inventory_knowledge` — 키워드 기반 지식 검색

### 2차 MVP — 주문/결제 정합성 모듈

- 멱등성 키 설계
- 결제 웹훅 검증
- 중복 결제 방어
- 부분 취소 / 환불 처리
- 망취소 처리

---

## 개발 환경 설정

### 사전 요구사항
- Java 21
- Gradle 8.x

### 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행 (Streamable HTTP 모드)
./gradlew bootRun

# 실행 (STDIO 모드 - Claude Code 연결용)
./gradlew bootRun --args='--spring.ai.mcp.server.stdio=true'
```

### Claude Code 연결 설정

프로젝트 루트의 `.claude/settings.json` 또는 `CLAUDE.md`에 추가:

```json
{
  "mcpServers": {
    "commerce-context": {
      "command": "java",
      "args": ["-jar", "/path/to/context-engine.jar"]
    }
  }
}
```

---

## 로드맵

| 단계 | 내용 | 상태 |
|------|------|------|
| 1단계 | 프로젝트 기반 구성 (MCP 서버 골격) | ✅ 완료 |
| 2단계 | 재고 도메인 모듈 구현 | ✅ 완료 |
| 3단계 | Claude Code / Cursor 연동 테스트 | ✅ 완료 |
| 4단계 | 주문/결제 정합성 모듈 구현 | ✅ 완료 |
| 5단계 | 정산 + 쿠폰 도메인 구현 | ✅ 완료 |
| 6단계 | 지식 저장소 DB 전환 + 관리자 API | 🔲 예정 |
| 7단계 | Beta 공개 | 🔲 예정 |

---

## 문서

- [PORTFOLIO_README.md](./PORTFOLIO_README.md) — 포트폴리오용 소개
- [../ARCHITECTURE.md](../ARCHITECTURE.md) — 상세 아키텍처
- [../CLAUDE.md](../CLAUDE.md) — AI 어시스턴트용 프로젝트 컨텍스트
- [DOMAIN_KNOWLEDGE_REFERENCE.md](./DOMAIN_KNOWLEDGE_REFERENCE.md) — 도메인 지식 원천
