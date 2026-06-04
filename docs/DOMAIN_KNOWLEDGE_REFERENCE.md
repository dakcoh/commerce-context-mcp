# Commerce Context MCP — 도메인 지식 레퍼런스

> 이 문서는 MCP 서버가 AI에게 주입하는 도메인 지식의 원천 레퍼런스다.
> 지식 모듈 추가·수정 시 이 문서를 먼저 업데이트한 후 코드에 반영한다.

---

## 목차

1. [재고 도메인](#1-재고-도메인)
2. [주문/결제 도메인](#2-주문결제-도메인)
3. [정산 도메인](#3-정산-도메인)
4. [쿠폰/프로모션 도메인](#4-쿠폰프로모션-도메인)
5. [범용 이커머스 기반 지식](#5-범용-이커머스-기반-지식)
6. [Java Spring 이커머스 구현 지식](#6-java-spring-이커머스-구현-지식)

---

## 1. 재고 도메인

### 1-1. 재고 상태 흐름

```
[가용 재고]
    │
    ├─ 주문 요청 → [예약 재고] (차감 전 임시 잠금)
    │                   │
    │                   ├─ 결제 성공 → [확정 차감] (실제 차감)
    │                   │
    │                   └─ 결제 실패 / 취소 → [복구] (예약 해제)
    │
    └─ 예약 없이 바로 차감 → ❌ 오버셀링 위험
```

**AI가 빠뜨리는 것**: 예약 단계 없이 바로 `stock - quantity`를 하면
결제 실패 시 재고가 복구되지 않고, 동시 요청 시 오버셀링이 발생한다.

### 1-2. 동시성 제어 전략

#### 낙관락 (Optimistic Lock)
```java
@Version
private Long version;
```
- **사용 시점**: 충돌이 드문 경우 (재고 여유 충분, 동시 주문 적음)
- **장점**: DB 락 없음, 성능 우수
- **단점**: 충돌 시 재시도 로직 필요
- **주의**: 재시도 횟수 제한 없으면 무한 루프 위험

#### 비관락 (Pessimistic Lock)
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
Product findByIdWithLock(@Param("id") Long id);
```
- **사용 시점**: 충돌이 잦은 경우 (인기 상품, 한정 수량)
- **장점**: 충돌 원천 차단
- **단점**: DB 커넥션 점유, 데드락 위험
- **주의**: 반드시 타임아웃 설정, 락 순서 일관성 유지

#### 분산락 (Distributed Lock — Redis)
```java
// Redisson 예시
RLock lock = redissonClient.getLock("inventory:" + productId);
lock.tryLock(3, 5, TimeUnit.SECONDS); // 대기 3초, 점유 5초
```
- **사용 시점**: 다중 서버 환경, MSA 구조
- **장점**: 서버 수평 확장 가능
- **단점**: Redis 의존성 추가, 락 해제 실패 시 처리 필요
- **주의**: 락 TTL 반드시 설정 (서버 장애 시 자동 해제)

#### 전략 선택 기준

| 상황 | 추천 전략 |
|------|----------|
| 단일 서버, 충돌 낮음 | 낙관락 |
| 단일 서버, 충돌 높음 (한정 수량) | 비관락 |
| 다중 서버 (MSA) | 분산락 (Redis) |

### 1-3. 멱등성 설계

**문제**: 네트워크 오류로 같은 차감 요청이 2번 오면 재고가 2번 차감된다.

**해결**: 요청마다 고유 키(idempotency key)를 부여해 중복 처리 방지.

```java
// 멱등성 키 예시
String idempotencyKey = "order:" + orderId + ":inventory:" + productId;

if (processedRequestRepository.exists(idempotencyKey)) {
    return cachedResult; // 이미 처리된 요청 → 저장된 결과 반환
}
// 처리 후 키 저장
processedRequestRepository.save(idempotencyKey, result, TTL_24H);
```

### 1-4. Saga 패턴 (주문-결제-재고 정합성)

**문제**: 주문 생성 → 결제 → 재고 차감 중 중간에 실패하면 데이터가 불일치한다.

**해결**: 각 단계마다 보상 트랜잭션(Compensating Transaction)을 정의한다.

```
정상 흐름:
주문 생성 → 재고 예약 → 결제 요청 → 결제 성공 → 재고 확정 차감 → 주문 완료

실패 시 보상:
결제 실패 → 재고 예약 해제 → 주문 취소

재고 예약 실패 → 주문 취소 (결제 미진행)
```

**주의**: 분산 트랜잭션(@Transactional)으로는 MSA 환경에서 정합성 보장 불가.
Saga 패턴 또는 Outbox 패턴 적용 필요.

### 1-5. AI가 자주 빠뜨리는 패턴 (체크리스트)

- [ ] 재고 예약 단계 없이 바로 차감
- [ ] 결제 실패 시 재고 복구 로직 누락
- [ ] 동시성 제어 없는 `stock - quantity`
- [ ] 멱등성 키 없는 차감 API
- [ ] 데드락 방어 없는 비관락
- [ ] 분산락 TTL 미설정
- [ ] Saga 보상 트랜잭션 미정의

---

## 2. 주문/결제 도메인

### 2-1. 결제 웹훅 처리

```
PG사 서버 → (HTTP POST) → 우리 서버 /webhook
                              ↓ 즉시 HTTP 200 응답
                              ↓ 비동기 큐 (Kafka / DB)
                              ↓ 워커 처리 (서명 검증 → 상태 업데이트)
```

**AI가 빠뜨리는 것**:
- 처리 완료 후 200 응답 → PG사 타임아웃(5~10초) → 재전송 루프 발생
- 웹훅 서명(HMAC-SHA256) 미검증 → 위조 웹훅 결제 완료 처리 가능 (보안 취약점)
- 중복 웹훅 미방어 → 결제 완료를 여러 번 처리

### 2-2. 중복 결제 방어 (3중 방어)

1. **주문 상태 검증**: 결제 요청 전 `PENDING` 상태 확인 (이미 `PAID`면 진행 X)
2. **멱등성 키**: PG사 API에 `Idempotency-Key` 헤더 전달
3. **PG 결제번호 중복 확인**: DB에 `pg_payment_id` 중복 체크

```java
// 낙관락으로 주문 상태 CAS 처리
@Version
private Long version;
```

### 2-3. 망취소 처리

**문제**: 결제 요청 후 응답 없음 → 실제 결제 여부 불명확

**해결 흐름**:
```
결제 요청 직전 → DB에 PENDING 기록
타임아웃 발생 → UNCERTAIN 상태로 전환
스케줄러 (1분 주기) → PG사 결제 조회 API 호출
→ 결제됨: PAID 확정 / 결제 안됨: CANCELLED 확정
→ N분 내 확정 불가: 자동 취소
```

**주의**: 망취소 상태 주문 방치 → 재고가 예약된 채 고객에게 상품 판매 불가

### 2-4. 부분 취소 / 환불

**쿠폰/포인트 병행 시 환불 금액 역산**:
```
주문 10,000원 - 쿠폰 1,000원 = 실결제 9,000원
5,000원 상품 부분 취소 시 환불액 = 5,000 × (9,000 / 10,000) = 4,500원
```

**처리 순서**:
1. 재고 복구 수량 계산 및 복구
2. 쿠폰/포인트 환불 금액 역산
3. 정산 완료 여부 확인 → 미완료면 PG사 취소 API / 완료면 별도 출금
4. 남은 주문 금액으로 쿠폰 사용 조건 재검증
5. 포인트 적립 금액 재계산

### 2-5. AI가 자주 빠뜨리는 패턴 (체크리스트)

- [ ] 웹훅 수신 즉시 HTTP 200 응답 (처리 완료 후 응답 X)
- [ ] 웹훅 서명(HMAC-SHA256) 검증 누락
- [ ] 웹훅 중복 수신 방어 (멱등성 처리)
- [ ] 결제 요청 전 주문 상태(PENDING) 검증
- [ ] PG사 API Idempotency-Key 헤더 전달
- [ ] 망취소 상황 (응답 타임아웃) 처리 로직
- [ ] 부분 취소 시 쿠폰/포인트 환불 금액 역산
- [ ] 정산 완료 후 환불 처리 분리
- [ ] 결제 실패 재시도 시 새 멱등성 키 생성
- [ ] 동시 결제 요청 낙관락(@Version) 처리

---

## 3. 정산 도메인

### 3-1. 정산 시점과 기준일 처리

**정산 시점 유형 비교**:

| 기준 | 장점 | 단점 |
|------|------|------|
| 구매 확정 | 반품 리스크 없음, 명확 | 정산이 가장 늦음 |
| 배송 완료 | 자동화 가능 | 반품 기간 중 정산 가능성 |
| 결제 완료 | 가장 빠름 | 반품/취소 발생 시 공제 복잡도 높음 |

**AI가 빠뜨리는 것**: 주말/공휴일 기준일 이월, 부분 취소된 주문의 정산 금액 재계산

### 3-2. 정산 공제 항목

```
정산 지급액 = 판매금액 - 수수료 - 반품금액 - 프로모션 분담금 - 쿠폰 분담금
```

- 이미 정산된 건의 반품 → 다음 주기 음수(-) 처리
- 공제 후 음수 → 다음 주기 이월 또는 별도 청구

### 3-3. 정산 배치 처리 원칙

- **멱등성**: 같은 날 배치 2번 실행 → 중복 정산 없어야 함
- **페이징**: 전체 조회 금지, Cursor 기반 페이징
- **실패 복구**: 재시작 지점 유지
- **정산 잠금**: 배치 실행 중 주문 상태 변경 방지

정산 상태 흐름: `UNSETTLED → SETTLEMENT_PENDING → SETTLED → DEDUCTED`

### 3-4. AI가 자주 빠뜨리는 패턴 (체크리스트)

- [ ] 정산 기준일 유형이 명확히 정의됐는가?
- [ ] 주말/공휴일 이월 처리가 있는가?
- [ ] 정산 배치에 멱등성 처리가 있는가?
- [ ] 이미 정산된 건의 반품 음수 공제가 처리됐는가?
- [ ] 정산 완료 후 금액/건수 정합성 검증이 있는가?
- [ ] 타임존(UTC/KST)이 일관되게 적용됐는가?

---

## 4. 쿠폰/프로모션 도메인

### 4-1. 쿠폰 유효성 검증 5가지

1. **유효 기간**: 서버 시간 기준 만료 여부
2. **사용 대상 조건**: 카테고리/상품/최소 주문금액 제한
3. **중복 사용 여부**: 상태 `ISSUED → USED` 확인
4. **1인 사용 한도**: 동일 쿠폰을 동일 유저가 여러 번 사용
5. **발급 대상 검증**: 타인 발급 쿠폰 사용 방지

**동시성 주의**: 쿠폰 상태 업데이트 시 낙관락 또는 CAS 처리 필수

### 4-2. 쿠폰 할인 금액 계산

- **정률 할인 캡**: `min(주문금액 × 할인율, 최대 할인 금액)` — 캡 누락이 가장 흔한 실수
- **복수 쿠폰 순서**: 정률 먼저, 정액 나중 (순서에 따라 최종 할인액 달라짐)
- **부분 취소 시**: 쿠폰 할인 비율 역산, 최소 주문금액 미달 시 쿠폰 무효화

### 4-3. 선착순 쿠폰 발급 동시성 제어

```
Redis DECR coupon:stock:{id}      → 0 미만이면 발급 거부
Redis SETNX coupon:issued:{id}:{userId}  → 중복 발급 방지
DB: userId + couponId UNIQUE 제약  → 최종 안전망
```

### 4-4. AI가 자주 빠뜨리는 패턴 (체크리스트)

- [ ] 쿠폰 유효 기간을 서버 시간 기준으로 검증하는가?
- [ ] 정률 할인 최대 할인 금액 캡(cap)이 처리됐는가?
- [ ] 선착순 쿠폰 발급 시 오버이슈 방지 로직이 있는가?
- [ ] userId + couponId에 UNIQUE 제약 조건이 있는가?
- [ ] 부분 취소 시 쿠폰 할인 금액 역산 및 무효화 처리가 있는가?

---

## 5. 범용 이커머스 기반 지식

도메인별 구현 팁과 별도로, 신규 쇼핑몰 구축과 아키텍처 리뷰에서 반복 사용하는
범용 이커머스 판단 기준을 `src/main/resources/knowledge/commerce.yml`에 정규화한다.

### 5-1. 정규화 원칙

각 지식은 단순한 한 줄 조언이 아니라 아래 정보를 독립 필드로 보관한다.

| 필드 | 의미 |
|------|------|
| `summary` | 빠르게 읽을 수 있는 핵심 결론 |
| `business-context` | 해당 규칙이 필요한 이커머스 유통 맥락 |
| `invariants` | 구현 방식이 바뀌어도 지켜야 하는 불변식 |
| `workflow` | 권장 업무·시스템 처리 순서 |
| `technical-guidance` | 범용적인 기술 구현 참고 |
| `failure-scenarios` | 누락 시 발생하는 장애와 데이터 불일치 |
| `checklist` | 코드 리뷰와 출시 전 검증 질문 |

### 5-2. 기본 지식 20개

| category | 지식 ID | 핵심 범위 |
|----------|---------|----------|
| `catalog` | `commerce-catalog-model` | Product, SKU, Offer 분리 |
| `pricing` | `commerce-pricing-money` | 금액 계산, 반올림, 가격 스냅샷 |
| `order` | `commerce-order-lifecycle` | 주문 상태 전이와 이력 |
| `inventory` | `commerce-inventory-availability` | 실재고, 예약, 판매 가능 수량 |
| `payment` | `commerce-payment-ledger` | 결제 거래 원장과 대사 |
| `fulfillment` | `commerce-fulfillment-reverse-logistics` | 배송, 반품, 역물류 |
| `promotion` | `commerce-promotion-allocation` | 할인 계산, 배부, 부담 주체 |
| `distribution` | `commerce-channel-seller-distribution` | 유통 채널, 판매자, 외부 동기화 |
| `settlement` | `commerce-settlement-reconciliation` | 판매자 정산과 자동 대사 |
| `operations` | `commerce-operational-integrity` | Outbox, DLQ, 감사 로그, 보정 |
| `customer` | `commerce-customer-identity` | 회원, 비회원, 주문자, 수령자 식별자 분리 |
| `checkout` | `commerce-cart-checkout` | 장바구니, 체크아웃 견적, 주문 확정 분리 |
| `search` | `commerce-search-discovery` | 검색 인덱스와 상품 원장 분리 |
| `claim` | `commerce-claim-cs` | 취소, 교환, 반품, 환불 클레임 흐름 |
| `security` | `commerce-security-privacy` | 개인정보, 권한 경계, 감사 로그 |
| `loyalty` | `commerce-loyalty-point-ledger` | 포인트 원장, 예약, 만료, 환불 |
| `membership` | `commerce-membership-tier-benefits` | 멤버십 등급 산정과 혜택 스냅샷 |
| `review` | `commerce-review-ugc-moderation` | 리뷰 작성 권한, 노출 상태, 신고 처리 |
| `subscription` | `commerce-subscription-recurring-order` | 구독 계약, 회차 주문, 반복 결제 |
| `operations` | `commerce-ops-slo-incident` | SLO, 알림, 장애 runbook, 보정 |

이 스키마는 YAML 검증 후 DB 지식 저장소의 초기 모델로 사용한다.

---

## 6. Java Spring 이커머스 구현 지식

유통 도메인 원칙과 별도로, Java 17+ + Spring Boot 기반 웹 백엔드에서 반복 사용하는
구현 기준을 `src/main/resources/knowledge/spring-commerce.yml`에 정규화한다.

| category | 지식 ID | 핵심 범위 |
|----------|---------|----------|
| `architecture` | `spring-commerce-modular-monolith` | 모듈형 모놀리스와 도메인 경계 |
| `transaction` | `spring-commerce-transaction-boundary` | 짧은 트랜잭션과 외부 API 분리 |
| `persistence` | `spring-commerce-jpa-consistency` | JPA Entity, DTO, N+1, cursor |
| `concurrency` | `spring-commerce-locking-idempotency` | 락, UNIQUE 제약, 멱등성 |
| `api` | `spring-commerce-validation-errors` | Bean Validation, 오류 코드, 마스킹 |
| `messaging` | `spring-commerce-events-outbox` | Outbox, 멱등 Consumer, DLQ |
| `cache` | `spring-commerce-cache-boundary` | Redis 캐시와 DB 원장 분리 |
| `batch` | `spring-commerce-scheduler-batch` | 스케줄러 중복 실행과 재시작 |
| `quality` | `spring-commerce-testing-observability` | Testcontainers, Micrometer, 로그 |
| `java-core` | `java-domain-modeling` | 값 객체, 타입 안정성, 도메인 검증 |
| `java-core` | `java-null-exception-boundary` | null, Optional, 예외 경계 |
| `java-core` | `java-collections-streams` | 컬렉션, Stream, 대량 처리 주의점 |
| `security` | `spring-security-authz` | 인증, 인가, 소유권 검증 |
| `configuration` | `spring-configuration-secrets` | 환경별 설정과 Secret 관리 |
| `persistence` | `spring-schema-migration` | Flyway/Liquibase, 무중단 스키마 변경 |
| `api` | `spring-api-pagination-idempotent-post` | cursor pagination, idempotent POST |
| `persistence` | `spring-database-index-isolation` | 인덱스, 실행 계획, 격리 수준 |
| `java-core` | `java-concurrency-executor-completablefuture` | Executor, CompletableFuture, 비동기 경계 |
| `integration` | `spring-resilience-retry-timeout` | timeout, retry, circuit breaker |
| `operations` | `spring-deployment-health-rollout` | readiness, smoke test, 롤백 지표 |

---

## 지식 모듈 추가 가이드

새로운 도메인 지식을 추가할 때는 아래 순서를 따른다.

```
1. 이 문서(docs/DOMAIN_KNOWLEDGE_REFERENCE.md)에 먼저 내용 정리
2. src/main/resources/knowledge/{domain}.yml에 데이터 추가
3. {Domain}KnowledgeService.java에 조회 로직 추가
4. {Domain}ContextTool.java에 @Tool 메서드 추가
5. docs/CLAUDE.md와 docs/ARCHITECTURE.md의 도구 목록 및 구조 설명 업데이트
```

## 현재 지식 현황

| 파일 | 범위 | 항목 수 |
|------|------|--------:|
| `inventory.yml` | 재고 예약, 동시성, 멱등성, Saga | 7 |
| `payment.yml` | 웹훅, 중복 결제, 망취소, 환불, 멱등성 | 6 |
| `settlement.yml` | 정산 시점, 공제, 배치, 정합성 | 5 |
| `coupon.yml` | 쿠폰 검증, 할인 계산, 발급, 프로모션 | 5 |
| `commerce.yml` | 범용 이커머스 도메인 설계 | 20 |
| `spring-commerce.yml` | Java/Spring 구현 지식 | 20 |

## YAML 스키마 검증

지식 파일을 수정한 뒤에는 아래 명령을 실행한다.

```powershell
.\gradlew.bat validateKnowledge --no-daemon
```

검증 항목:
- 전체 지식 ID 전역 중복 금지
- `id`는 소문자 영문, 숫자, 하이픈 형식
- 도메인별 허용 category만 사용
- 필수 본문 필드와 리스트 필드 누락 금지
- 태그 비어 있음과 태그 중복 금지

## 다음 보강 후보

- 추천/개인화: 추천 후보 생성, 랭킹, 필터링, 노출 이력, A/B 테스트
- B2B/도매: 사업자 가격, 견적, 승인, 세금계산서, 거래처별 한도
- 글로벌 커머스: 다국어, 다통화, 관세, 국가별 배송/반품 정책
- 관리자/운영 도구: 권한 분리, 수동 보정 워크플로우, 승인 체계
- 지식 품질 관리: 검색 품질 평가셋, 권장 키워드 누락 감지, 문서 항목 수 자동 대사

## 저장소 운영 판단

현재 단계에서는 DB 지식 저장소보다 YAML 유지가 적합하다.
지식이 정적이고 Git 리뷰 대상이며, MCP 서버를 가볍게 배포할 수 있기 때문이다.
DB 전환은 운영자가 비개발 방식으로 자주 수정해야 하거나 사용자별 지식 분리가 필요해질 때 검토한다.

---

## 버전 이력

| 버전 | 날짜 | 내용 |
|------|------|------|
| 0.1.0 | 2026-06-02 | 초안 작성 — 재고 도메인 |
| 0.2.0 | 2026-06-02 | 결제 도메인 추가 (웹훅, 중복결제, 망취소, 부분환불, 멱등성) |
| 0.3.0 | 2026-06-02 | 정산 도메인 추가 (시점, 공제, 배치, 정합성) |
| 0.4.0 | 2026-06-02 | 쿠폰/프로모션 도메인 추가 (검증, 계산, 선착순 발급, 규칙 엔진) |
| 0.5.0 | 2026-06-02 | 범용 이커머스 정규화 지식 10개 추가 (commerce.yml) |
| 0.6.0 | 2026-06-02 | Java Spring 이커머스 구현 지식 9개 추가 (spring-commerce.yml) |
| 0.7.0 | 2026-06-04 | 범용 이커머스 지식 5개 추가 (고객, 체크아웃, 검색, 클레임, 보안) |
| 0.8.0 | 2026-06-04 | Java/Spring 구현 지식 6개 추가 (값 객체, 예외, Stream, 보안, 설정, 마이그레이션) |
| 0.9.0 | 2026-06-04 | 범용 이커머스 지식 5개 추가 (포인트, 멤버십, 리뷰, 구독, 운영 SLO) |
| 1.0.0 | 2026-06-04 | Java/Spring 구현 지식 5개 추가 (API, DB, 비동기, 회복성, 배포) |
