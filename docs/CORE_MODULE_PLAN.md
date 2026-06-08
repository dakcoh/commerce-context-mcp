# Core Module Separation Plan

> 목표: Commerce Context MCP를 MCP 서버뿐 아니라 Java/Spring 프로젝트에서 재사용 가능한 지식 라이브러리로 고도화한다.

## 현재 상태 (2026-06-08 기준)

**Phase 1~4 완료.** core 패키지 경계와 구조화 API가 모두 구현됐다.

| Phase | 내용 | 상태 |
|-------|------|------|
| Phase 1 | Core 모델 추가 (KnowledgeEntry·Query·SearchResult·Repository·SearchService·Renderer) | ✅ 완료 |
| Phase 2 | YAML 정규화 어댑터 (KnowledgeEntryMapper + YamlKnowledgeRepository) | ✅ 완료 |
| Phase 3 | 검색 서비스 통합 (DefaultKnowledgeSearchService, 스코어링) | ✅ 완료 |
| Phase 4 | Markdown renderer 분리, MCP Tool 교체, 구 서비스 삭제 | ✅ 완료 |
| Phase 5 | Gradle 멀티 모듈 분리 | 🔲 선택, 필요 시 진행 |

## 배경

현재 프로젝트는 다음 패키지 경계로 구조화됐다.

```
core/       순수 Java 인터페이스 + 모델 (Spring 의존 없음)
repository/ YamlKnowledgeRepository — YAML → KnowledgeEntry 변환
service/    DefaultKnowledgeSearchService, MarkdownKnowledgeRenderer
tool/       @Tool 클래스 — KnowledgeSearchService + KnowledgeRenderer만 의존
```

Phase 5 (Gradle 멀티 모듈)는 `context-engine-core`를 Maven Central에 독립 배포할 필요가 생길 때 진행한다.

## 목표

- MCP 서버 기능은 유지한다.
- 지식 검색 결과를 Markdown 문자열뿐 아니라 구조화 객체로 제공한다.
- Java/Spring 애플리케이션에서 MCP 없이도 지식 검색 라이브러리로 사용할 수 있게 한다.
- YAML 저장 방식을 유지하되, 추후 DB/벡터 검색으로 교체 가능한 저장소 경계를 만든다.

## 비목표

- 이번 단계에서 DB 저장소를 도입하지 않는다.
- 관리자 CRUD API를 만들지 않는다.
- 벡터 검색이나 임베딩 검색을 바로 도입하지 않는다.
- 현재 33개 MCP 도구의 이름과 기본 동작을 깨지 않는다.

## 현재 문제

### 1. Markdown 중심 반환

현재 `*KnowledgeService`는 대부분 `String`을 반환한다.
MCP 응답에는 편하지만, 라이브러리 사용자는 아래와 같은 구조화 데이터를 얻기 어렵다.

- knowledge id
- category
- title
- tags
- matched fields
- checklist
- source domain

### 2. 저장소와 서비스의 직접 결합

서비스가 `*KnowledgeProperties`에 직접 의존한다.
YAML에서 DB로 바꾸려면 서비스 내부를 고쳐야 한다.

### 3. MCP 서버 의존성

현재 루트 프로젝트는 Spring Boot web, actuator, Spring AI MCP 서버 의존성을 포함한다.
다른 프로젝트가 지식 검색만 사용하고 싶어도 서버 의존성이 같이 따라온다.

### 4. 도메인별 반복

Inventory, Payment, Settlement, Coupon 서비스는 검색, 카테고리 필터, Markdown 포맷 로직이 유사하다.
정책 변경 시 여러 파일을 함께 수정해야 한다.

## 제안 구조

```text
context-engine
├── context-engine-core
│   ├── KnowledgeEntry
│   ├── KnowledgeDocument
│   ├── KnowledgeQuery
│   ├── KnowledgeSearchResult
│   ├── KnowledgeRepository
│   ├── KnowledgeSearchService
│   └── MarkdownKnowledgeRenderer
│
├── context-engine-spring
│   ├── YAML ConfigurationProperties
│   ├── YamlKnowledgeRepository
│   └── AutoConfiguration 후보
│
└── context-engine-mcp-server
    ├── @Tool classes
    ├── Spring AI MCP config
    ├── application.yml
    └── npm/JAR 배포
```

초기에는 실제 멀티 모듈로 바로 나누지 않고, 패키지 경계를 먼저 만들 수 있다.

```text
com.commerce.context_engine.core
com.commerce.context_engine.spring
com.commerce.context_engine.tool
```

패키지 경계가 안정되면 Gradle 멀티 모듈로 분리한다.

## Core API 초안

### KnowledgeEntry

```java
public record KnowledgeEntry(
        String domain,
        String id,
        String category,
        String title,
        String summary,
        String content,
        List<String> sections,
        List<String> checklist,
        List<String> tags
) {
}
```

도메인별 YAML 스키마가 다르더라도 core에서는 하나의 정규화 모델로 다룬다.

### KnowledgeQuery

```java
public record KnowledgeQuery(
        String keyword,
        String domain,
        String category,
        int limit
) {
}
```

### KnowledgeSearchResult

```java
public record KnowledgeSearchResult(
        KnowledgeEntry entry,
        List<String> matchedFields,
        int score
) {
}
```

초기 score는 단순 규칙 기반으로 둔다.

- title match: +5
- tag match: +4
- category match: +3
- content/body match: +1

### KnowledgeRepository

```java
public interface KnowledgeRepository {
    List<KnowledgeEntry> findAll();
    List<KnowledgeEntry> findByDomain(String domain);
    List<KnowledgeEntry> findByCategory(String domain, String category);
}
```

YAML, DB, 벡터 검색 저장소는 이 인터페이스 뒤로 숨긴다.

### KnowledgeSearchService

```java
public interface KnowledgeSearchService {
    List<KnowledgeSearchResult> search(KnowledgeQuery query);
    List<KnowledgeEntry> getByDomain(String domain);
    List<KnowledgeEntry> getByCategory(String domain, String category);
}
```

### MarkdownKnowledgeRenderer

```java
public interface KnowledgeRenderer {
    String renderEntry(KnowledgeEntry entry);
    String renderSearchResults(List<KnowledgeSearchResult> results);
    String renderChecklist(List<KnowledgeEntry> entries);
}
```

MCP 도구는 core 검색 결과를 받은 뒤 Markdown renderer를 사용한다.
라이브러리 사용자는 renderer 없이 구조화 객체를 바로 사용할 수 있다.

## MCP Tool 변경 방향

현재:

```text
Tool -> DomainKnowledgeService -> String
```

목표:

```text
Tool -> KnowledgeSearchService -> List<KnowledgeSearchResult>
     -> MarkdownKnowledgeRenderer -> String
```

도구 이름과 외부 응답 형식은 유지한다.
내부 구현만 구조화 API 기반으로 바꾼다.

## 라이브러리 사용 예시

Spring 앱에서:

```java
@RequiredArgsConstructor
@Service
public class ReviewAssistant {

    private final KnowledgeSearchService knowledgeSearchService;

    public List<KnowledgeSearchResult> findPaymentRisks() {
        return knowledgeSearchService.search(new KnowledgeQuery(
                "웹훅 중복 결제 멱등성",
                "payment",
                null,
                5
        ));
    }
}
```

순수 Java 라이브러리 형태:

```java
KnowledgeRepository repository = new ClasspathYamlKnowledgeRepository();
KnowledgeSearchService service = new DefaultKnowledgeSearchService(repository);

List<KnowledgeSearchResult> results = service.search(
        new KnowledgeQuery("재고 오버셀링", "inventory", null, 3)
);
```

## 단계별 진행 계획

### Phase 1. Core 모델 추가 ✅

- `KnowledgeEntry` — 6개 도메인 스키마를 통합한 정규화 지식 단위
- `KnowledgeQuery` — keyword·domain·category·limit + 팩토리 메서드
- `KnowledgeSearchResult` — entry·matchedFields·score
- `KnowledgeRepository` — findAll·findByDomain·findByCategory
- `KnowledgeSearchService` — search·getByDomain·getByCategory
- `KnowledgeRenderer` — render·renderAll·renderSearchResults·renderChecklist

### Phase 2. YAML 정규화 어댑터 ✅

- `KnowledgeEntryMapper` (package-private) — 6개 도메인 Properties.Item → KnowledgeEntry
- `YamlKnowledgeRepository` — 6개 YAML을 단일 `KnowledgeRepository`로 통합

### Phase 3. 검색 서비스 통합 ✅

- `DefaultKnowledgeSearchService` — 스코어링 기반 검색 (title +5 / tags +4 / category +3 / summary +2 / content·sections +1)
- 전체 도메인 통합 검색 지원 (`KnowledgeQuery.ofKeyword`)
- 구 도메인별 서비스 6개 삭제

### Phase 4. Markdown renderer 분리 ✅

- `MarkdownKnowledgeRenderer` — simple(summary 없음) / rich(summary 있음) 분기 렌더링
- 모든 Tool 클래스 교체 (KnowledgeSearchService + KnowledgeRenderer 의존)
- `UnifiedSearchTool` 추가 — 전체 도메인 통합 검색 (tool 총 33개)

### Phase 5. Gradle 모듈 분리

선택 사항. `context-engine-core`를 Maven Central에 독립 배포할 필요가 생길 때 진행한다.

완료 기준:
- `context-engine-core`가 Spring Boot 없이 빌드됨
- `context-engine-mcp-server`가 core에 의존함

## 테스트 현황 ✅

| 테스트 | 수 | 검증 내용 |
|--------|---|----------|
| `YamlKnowledgeRepositoryTest` | 8 | 전체 항목 수, 도메인 필터, sections 구조, 필수 필드 |
| `DefaultKnowledgeSearchServiceTest` | 14 | 키워드 검색, 스코어 정렬, 도메인 필터, limit, matchedFields, 다중 단어·띄어쓰기 무관 검색 |
| `MarkdownKnowledgeRendererTest` | 12 | simple/rich 렌더링, 섹션 헤더, numbered/bullet/checkbox |
| `UnifiedSearchToolTest` | 5 | 전체 도메인 통합 검색, 빈 키워드, 결과 없음 처리 |
| MCP Tool 테스트 (6종) | 35 | 각 도구 응답 내용·포맷 검증 |

## 리스크

### 응답 형식 회귀

MCP 도구의 Markdown 출력이 바뀌면 기존 테스트와 사용자 경험이 흔들릴 수 있다.
기존 tool 테스트를 유지하고 renderer 테스트를 추가한다.

### 과한 추상화

지식 저장소가 아직 YAML뿐인데 Repository/Renderer를 과하게 만들 수 있다.
초기 인터페이스는 작게 유지하고 DB/벡터 검색을 예상한 메서드는 추가하지 않는다.

### 일정 리스크

공개 배포 직후에는 Gradle 멀티 모듈까지 한 번에 진행하지 않는다.
먼저 패키지 경계와 구조화 API만 만들고, 사용성 안정화 후 멀티 모듈로 분리한다.

## 권장 우선순위

1. Core 모델과 검색 결과 객체 추가
2. YAML to KnowledgeEntry mapper 추가
3. 통합 검색 서비스 추가
4. Markdown renderer 추가
5. MCP 도구 내부 구현 교체
6. 안정화 후 Gradle 멀티 모듈 분리

## 최종 판단

현재 프로젝트는 MCP 서버로는 이미 충분히 동작 가능한 구조다.
다만 장기적으로 Java/Spring 프로젝트에서 지식 라이브러리처럼 쓰려면 core API 분리가 필요하다.

공개 배포 이후 고도화 목표라면 **Gradle 멀티 모듈은 미루고, core 패키지 + 구조화 API부터 도입**하는 것이 가장 안전하다.
