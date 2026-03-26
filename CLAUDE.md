# CLAUDE.md

## DDD 레이어 구조

```
domain/         — 순수 도메인 로직. Spring/JPA 의존성 최소화. Repository 인터페이스 포함.
application/    — 유즈케이스(커맨드/쿼리). 도메인과 인프라를 조율.
infrastructure/ — JPA 구현체, State Machine, Kafka, Redis 등 기술적 관심사.
presentation/   — REST Controller, 요청/응답 DTO.
```

### 레이어 규칙

- `domain` → 다른 레이어에 의존하지 않는다
- `application` → `domain`에만 의존한다
- `infrastructure` → `domain`, `application`에 의존한다
- `presentation` → `application`에만 의존한다 (`domain` 직접 참조 금지)

### 핵심 설계 결정

- **Order는 Aggregate Root** — `OrderHistory`는 `Order`를 통해서만 생성된다
- **Order.create()** — private 생성자, 팩토리 메서드로만 생성
- **Order.changeStatus()** — Spring Statemachine Action에서만 호출 (전이 유효성은 Statemachine이 보장)
- **OrderRepository** 인터페이스는 `domain` 레이어, 구현체는 `infrastructure` 레이어 (DIP)
- **낙관적 잠금** — `Order`의 `@Version` 필드로 동시성 제어

## TDD 워크플로우

**Red → Green → Refactor** 사이클을 준수한다.

```
1. 실패하는 테스트 먼저 작성
2. 테스트를 통과하는 최소한의 구현
3. 리팩토링 (테스트는 여전히 통과해야 함)
```

### 테스트 종류별 전략

| 레이어 | 테스트 종류 | 어노테이션 |
|---|---|---|
| `domain` | 순수 단위 테스트 | 없음 (Spring 컨텍스트 불필요) |
| `infrastructure/statemachine` | 경량 통합 테스트 | `@SpringBootTest @ActiveProfiles("test")` |
| `infrastructure/persistence` | JPA 슬라이스 테스트 | `@DataJpaTest @ActiveProfiles("test")` |
| `application` | 단위 테스트 + Mock | `@ExtendWith(MockitoExtension::class)` |
| `presentation` | 웹 슬라이스 테스트 | `@WebMvcTest` |

### 테스트 파일 위치

구현 파일과 동일한 패키지 구조를 `src/test/` 아래에 유지한다.

```
src/main/kotlin/.../domain/order/Order.kt
src/test/kotlin/.../domain/order/OrderTest.kt  ← 동일 패키지
```