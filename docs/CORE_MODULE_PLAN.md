# Core Module Separation Plan

> 목표: Commerce Context Engine을 MCP 서버뿐 아니라 Java/Spring 프로젝트에서 재사용 가능한 지식 라이브러리로 고도화한다.

## 현재 우선순위

외부 사용자가 더 편하게 쓰게 만드는 것이 현재 1순위다.
따라서 core 모듈 분리는 공개 배포 직후의 필수 작업이 아니라 장기 고도화 계획으로 둔다.

현재 외부 사용성은 npm 배포로 1차 완료됐다.
이후에는 릴리즈 안정화와 설치 문서 보강을 진행하면서 core API 분리를 검토한다.

## 배경

현재 프로젝트는 Spring AI MCP Server로 동작하기에 충분한 구조를 갖고 있다.
다만 서비스 계층이 바로 Markdown 문자열을 반환하고, Spring Boot 웹/MCP 의존성이 루트 모듈에 묶여 있어 다른 Java 프로젝트에서 라이브러리처럼 사용하기에는 무겁다.

현재는 외부 사용자가 `npx`와 MCP 설정만으로 사용할 수 있는 상태다.
core 모듈 분리는 npm 배포 안정화 후 구조화 API가 필요해지는 시점에 적용한다.

## 목표

- MCP 서버 기능은 유지한다.
- 지식 검색 결과를 Markdown 문자열뿐 아니라 구조화 객체로 제공한다.
- Java/Spring 애플리케이션에서 MCP 없이도 지식 검색 라이브러리로 사용할 수 있게 한다.
- YAML 저장 방식을 유지하되, 추후 DB/벡터 검색으로 교체 가능한 저장소 경계를 만든다.

## 비목표

- 이번 단계에서 DB 저장소를 도입하지 않는다.
- 관리자 CRUD API를 만들지 않는다.
- 벡터 검색이나 임베딩 검색을 바로 도입하지 않는다.
- 현재 31개 MCP 도구의 이름과 기본 동작을 깨지 않는다.

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

### Phase 1. Core 모델 추가

- `KnowledgeEntry`
- `KnowledgeQuery`
- `KnowledgeSearchResult`
- `KnowledgeRepository`
- `KnowledgeSearchService`
- `KnowledgeRenderer`

완료 기준:
- 기존 서비스와 별개로 core 타입 컴파일 가능
- 테스트에서 core 검색 결과 객체 검증 가능

### Phase 2. YAML 정규화 어댑터

- 기존 `*KnowledgeProperties`를 `KnowledgeEntry`로 변환하는 mapper 추가
- 모든 YAML 지식을 단일 `KnowledgeRepository`로 모으는 구현 추가

완료 기준:
- 전체 지식 항목 수를 core repository에서 조회 가능
- domain/category 필터 테스트 통과

### Phase 3. 검색 서비스 통합

- 단순 contains 검색을 core `DefaultKnowledgeSearchService`로 이동
- match field와 score를 제공
- 빈 키워드 처리 정책 통일

완료 기준:
- `inventory`, `payment`, `commerce`, `spring-commerce` 검색이 core API로 동작
- 기존 도메인별 검색 테스트 통과

### Phase 4. Markdown renderer 분리

- 기존 서비스의 Markdown 포맷 로직을 renderer로 이동
- MCP 도구는 renderer 결과만 반환

완료 기준:
- MCP 도구 응답 형식 유지
- 구조화 API와 Markdown API가 모두 테스트됨

### Phase 5. Gradle 모듈 분리

선택 사항이다.
패키지 경계가 안정된 뒤 진행한다.

완료 기준:
- `context-engine-core`가 Spring Boot 없이 빌드됨
- `context-engine-mcp-server`가 core에 의존함

## 테스트 전략

추가할 테스트:

- `KnowledgeEntryMapperTest`
- `YamlKnowledgeRepositoryTest`
- `DefaultKnowledgeSearchServiceTest`
- `MarkdownKnowledgeRendererTest`
- 기존 MCP tool 테스트 유지

검증할 항목:

- 전체 지식 항목 수
- domain/category 필터
- keyword 검색
- score 정렬
- 빈 키워드 처리
- Markdown 출력 호환성

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
