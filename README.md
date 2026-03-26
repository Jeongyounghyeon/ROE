# 🛒 State Machine 기반 커머스 주문 시스템 (Order Management System)

대규모 트래픽 환경에서 데이터 정합성을 보장하고 복잡한 주문 생명주기(Lifecycle)를 안전하게 관리하기 위해 **유한 상태 기계(FSM, Finite State Machine)** 패턴을 적용한 주문/결제
백엔드 시스템입니다.

## 🚀 프로젝트 개요 및 아키텍처 목표

커머스 도메인의 핵심인 '주문'과 '결제'는 상태(Status)와 상태 간의 전이(Transition)가 매우 명확하며, 비정상적인 전이는 금전적인 사고 및 치명적인 버그로 이어집니다. 본 프로젝트는 상태 머신을
도입하여 다음과 같은 엔지니어링 목표를 달성합니다.

- **안전성 (Safety)**: 정의되지 않은 상태 전이 원천 차단 (예: '결제 대기' -> '배송 완료' 등 임의 조작 방지)
- **추적성 (Traceability)**: 모든 이벤트 트리거와 상태 변경 이력을 보관하여 CS 처리 및 모니터링/디버깅 역량 강화
- **확장성 (Scalability)**: 부분 취소, 교환, 환불 등 신규 비즈니스 프로세스 추가 시 기존 상태 플로우의 훼손 최소화
- **정합성 (Consistency)**: 대규모 동시 접속 환경에서도 낙관적/비관적 잠금 및 분산 락을 이용한 동시성 제어 및 철저한 로직 검증

## 💻 기술 스택 (Tech Stack)

대규모 트래픽 처리와 엔터프라이즈급 비즈니스 정합성을 최우선으로 고려하여 아키텍처를 구성했습니다.

### Backend (Core)

- **Language**: `Kotlin 1.9+` (Null 안정성 및 Coroutine을 활용한 리소스 최적화)
- **Framework**: `Spring Boot 3.x`
- **State Machine**: `Spring Statemachine` (상태 제어 및 트랜잭션 관리 통합)
- **ORM**: `Spring Data JPA`

### Database & Infrastructure

- **RDBMS**: `PostgreSQL` (안정적인 동시성 제어 및 예외 상황 방어)
- **Message Broker**: `Kafka` (이벤트 기반 비동기 아키텍처 - EDA 구성)
- **Cache & Lock**: `Redis` (초고속 재고 임시 차감 통제 및 글로벌 분산 락 구현)

## 🔄 주요 상태(Status) 다이어그램

| 상태명                  | 설명                  | 비고                        |
|:---------------------|:--------------------|:--------------------------|
| `PENDING_PAYMENT`    | 결제 대기 (주문 생성 직후)    |                           |
| `PAYMENT_PROCESSING` | 플랫폼(PG) 결제 승인 요청 중  | 외부 API 통신 대기 (네트워크 지연 고려) |
| `PAID`               | 결제 승인 완료            | 결제 검증 및 실재고 차감 확정         |
| `PREPARING`          | 상품 준비 중 (판매자 확인 발주) |                           |
| `SHIPPED`            | 배송 중 (택배사 인계)       |                           |
| `DELIVERED`          | 배송 및 고객 수령 완료       | 구매 확정 대기 상태 진입            |
| `CANCELED`           | 결제 정상 취소 완료         | 재고 롤백 (종결 상태)             |
| `REFUNDING`          | 결제 완료 이후 환불 처리 진행 중 | PG 취소 응답 대기               |
| `REFUNDED`           | 환불 최종 완료            | (종결 상태)                   |
| `FAILED`             | 재고 부족, 타임아웃 등 결제 실패 | 잔여 리소스 롤백 (종결 상태)         |

### 🎯 핵심 상태 전이(Transition) 규칙

| 현재 상태 (From)         | 발생 이벤트 (Event)   | 다음 상태 (To)           | 트리거되는 핵심 비즈니스 로직                                     |
|:---------------------|:-----------------|:---------------------|:--------------------------------------------------------|
| `PENDING_PAYMENT`    | `REQUEST_PAY`    | `PAYMENT_PROCESSING` | 재고 임시 차감 체크, 본결제 데이터 세팅                               |
| `PAYMENT_PROCESSING` | `PAY_SUCCESS`    | `PAID`               | 웹훅 검증 저장, 최종 실재고 차감 확정                                |
| `PAID`               | `ACCEPT_ORDER`   | `PREPARING`          | DB상 판매자 송장 정보 발행 세팅                                   |
| `PREPARING`          | `DISPATCH`       | `SHIPPED`            | 운송장 배송 시스템 연동 및 발송 알림                                 |
| `PENDING_PAYMENT`    | `CANCEL_REQUEST` | `CANCELED`           | 임시 차감 재고 복구, 취소 이력 기록                                 |
| `PAID`               | `CANCEL_REQUEST` | `REFUNDING`          | PG 환불 API 호출, 환불 처리 시작                                |
| `PREPARING`          | `CANCEL_REQUEST` | `REFUNDING`          | PG 환불 API 호출, 판매자 발주 취소                               |
| `DELIVERED`          | `CANCEL_REQUEST` | `REFUNDING`          | PG 환불 API 호출, 구매 확정 전 환불                              |
| `PAYMENT_PROCESSING` | `CANCEL_REQUEST` | (불가)                 | PG 응답 대기 중 타이밍 이슈 위험 — PAY_SUCCESS/PAY_FAIL 이후 처리    |
| `SHIPPED`            | `CANCEL_REQUEST` | (불가)                 | 택배사 인계 후 물리적 회수 불가 — 수령 후 DELIVERED 상태에서 환불 요청      |

## 🛠 아키텍처 핵심 구현 포인트

1. **상태 머신 (FSM) 엔진 결합**
    - 도메인 엔티티 내부에 파편화되어 있던 `if/else` 상태 분기 로직을 전면 제거. 상태 머신 설정(Config)을 통해 전이 가능한 합법적 노드인지 엔진 단에서 1차 검증합니다.
2. **동시성 및 멱등성 보장 (Concurrency & Idempotency)**
    - 동일 주문/결제 트랜잭션에 대한 중복 웹훅 수신 시 상태 엔진 레벨에서 무시(Idempotent) 처리하여 중복 결제를 차단합니다.
    - 다중 노드 환경에서 '어드민 강제 취소'와 '사용자 결제 완료'가 동시에 일어날 경우 등을 대비해 DB 레벨의 낙관적 잠금(`@Version`)으로 강력 방어합니다.
3. **이벤트 주도 아키텍처 적용 (EDA, Event-Driven Architecture)**
    - 비즈니스 핵심 상태가 변경(`PAID`, `SHIPPED` 등)되면 도메인 이벤트를 발행합니다. 알림톡, 정산, 통계 모듈이 이를 비동기적으로 구독하여 장애 격리 및 시스템 간 느슨한 결합(Loose
      Coupling)을 이끌어냅니다.
4. **이력 데이터 영속성 (Audit Logging)**
    - `order_histories` 테이블을 `Append-only` 구조로 운영하여 모든 시스템 상의 상태 변경 내역(원인 Event, 발생 시간, 전/후 상태, 처리 사유 등)을 안전하게 영구 기록합니다.
