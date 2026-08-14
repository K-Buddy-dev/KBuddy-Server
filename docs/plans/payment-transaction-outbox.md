# 결제 트랜잭션 및 외부 작업 분리 계획

## 1. 문서 목적

결제와 예약의 핵심 DB 상태 변경을 안전하게 처리하고, 채팅방 생성과 알림 발송 같은 후속 작업을 DB 트랜잭션에서 분리한다.

이 단계는 다음 문서의 작업이 완료된 후 진행한다.

1. [동시성 제어](./payment-booking-concurrency-control.md)
2. [멱등성 보장](./payment-booking-idempotency.md)
3. [예약 및 결제 만료 처리](./payment-booking-expiration.md)

## 2. 현재 문제

현재 결제 승인 흐름은 하나의 서비스 메서드에서 다음 작업을 수행한다.

```text
Payment 상태 변경
→ Booking 상태 변경
→ ChatRoom 생성
→ Notification 저장
→ FCM 발송
```

Payment, Booking, ChatRoom, Notification의 저장은 DB 트랜잭션에 참여할 수 있다. 그러나 FCM은 외부 시스템이므로 DB 트랜잭션에 참여하지 않는다.

### 2.1 DB 롤백과 외부 알림 불일치

```text
1. Payment와 Booking 변경
2. FCM 결제 완료 알림 전송 성공
3. DB 커밋 실패
4. DB 변경 롤백
```

최종 결과:

```text
사용자가 받은 알림 = 결제 완료
DB 상태 = 결제 대기
```

이미 전송된 외부 알림은 DB 롤백으로 취소할 수 없다.

### 2.2 긴 트랜잭션

트랜잭션 안에서 외부 API를 기다리면 DB 연결과 잠금을 오래 점유한다. 결제 동시성이 높아지면 잠금 대기와 타임아웃이 증가할 수 있다.

## 3. 트랜잭션 경계

### 3.1 한 트랜잭션에서 처리할 작업

다음은 관련 상태가 모두 성공하거나 모두 실패해야 한다.

- 결제 승인: Payment와 Booking 상태 변경
- 결제 및 예약 만료: Payment, Booking, Slot 상태 변경
- 미결제 예약 취소: Payment, Booking, Slot 상태 변경
- 환불 확정: Payment와 Booking 상태 변경
- 업무 이벤트를 Outbox에 저장

### 3.2 트랜잭션 밖에서 처리할 작업

- FCM 발송
- 이메일 발송
- 외부 PG API 호출
- 채팅방 생성 후속 처리
- 재시도가 필요한 외부 작업

채팅방을 결제 승인과 반드시 같은 DB 커밋으로 묶어야 하는 업무 요구가 없다면 Outbox 작업자로 분리한다.

## 4. 권장 구조: Transactional Outbox

### 4.1 주 트랜잭션

```text
Payment 상태 변경
Booking 상태 변경
OutboxEvent 저장
커밋
```

업무 상태와 이벤트가 같은 트랜잭션에 저장되므로 다음 두 상태 중 하나만 존재한다.

```text
업무 상태 변경 + 이벤트 저장 모두 성공
또는
업무 상태 변경 + 이벤트 저장 모두 롤백
```

### 4.2 이벤트 작업자

```text
PENDING 이벤트 조회
→ 처리 권한 획득
→ 채팅방 find-or-create
→ 알림 저장
→ FCM 발송
→ COMPLETED 기록
```

실패하면 재시도 횟수와 다음 실행 시각을 기록한다.

## 5. Outbox 테이블 설계

예시:

```text
outbox_event
- id
- event_key              UNIQUE
- event_type
- aggregate_type
- aggregate_id
- payload
- status
- retry_count
- next_retry_at
- last_error
- created_at
- processed_at
- version
```

상태 예시:

```text
PENDING
PROCESSING
COMPLETED
FAILED
```

이벤트 키 예시:

```text
PAYMENT_CONFIRMED:{paymentId}
PAYMENT_EXPIRED:{paymentId}
PAYMENT_REFUNDED:{paymentId}
```

`event_key` 유니크 제약조건으로 같은 업무 이벤트가 중복 저장되는 것을 막는다.

## 6. 이벤트 작업자 구현

### 6.1 이벤트 조회

다중 서버에서 안전하게 가져가기 위해 PostgreSQL의 `FOR UPDATE SKIP LOCKED` 사용을 고려한다.

```text
status = PENDING
AND next_retry_at <= now
ORDER BY created_at, id
LIMIT 100
FOR UPDATE SKIP LOCKED
```

### 6.2 처리 단위

한 이벤트의 실패가 전체 배치를 롤백하지 않도록 이벤트별 처리 경계를 분리한다.

```text
배치 조회
→ 이벤트 1 처리 및 결과 저장
→ 이벤트 2 처리 및 결과 저장
→ ...
```

### 6.3 재시도 정책

지수 백오프를 적용한다.

```text
1회 실패 → 1분 후
2회 실패 → 5분 후
3회 실패 → 30분 후
4회 실패 → 2시간 후
최대 횟수 초과 → FAILED
```

재시도 가능한 오류와 재시도해도 해결되지 않는 오류를 구분한다.

- 네트워크 타임아웃: 재시도
- FCM 일시 오류: 재시도
- 잘못된 payload: 즉시 FAILED
- 유효하지 않은 FCM 토큰: 토큰 비활성화 후 해당 발송 종료

### 6.4 후속 작업 멱등성

이벤트 작업자는 같은 이벤트가 다시 실행되어도 안전해야 한다.

- 채팅방: `bookingId` 기준 `find-or-create`
- 알림: 이벤트 키와 수신자 조합으로 유니크 처리
- FCM: 발송 기록 또는 알림 상태로 중복 최소화

외부 시스템 호출이 성공한 직후 프로세스가 중단될 수 있으므로 완벽한 exactly-once 전송보다 at-least-once 처리와 멱등한 수신·후속 작업을 목표로 한다.

## 7. 외부 PG 호출 경계

PG API를 DB 트랜잭션 안에서 오래 기다리지 않는다.

권장 흐름:

```text
1. 짧은 DB 트랜잭션
   - 결제 처리 시작 상태 저장
   - 멱등키 저장

2. DB 트랜잭션 밖
   - PG 승인 또는 취소 API 호출

3. 짧은 DB 트랜잭션
   - Payment 잠금
   - PG 응답 검증
   - Payment와 Booking 최종 상태 변경
   - Outbox 이벤트 저장

4. 이벤트 작업자
   - 채팅방과 알림 처리
```

PG 응답을 받지 못한 경우 새 결제를 만들지 않고 같은 멱등키로 재시도하거나 PG 조회 API로 결과를 확인한다.

## 8. 구현 대상 파일

| 대상 | 변경 내용 |
|---|---|
| `PaymentService` | 핵심 상태 변경과 후속 작업 분리 |
| 신규 `OutboxEvent` | 이벤트 데이터와 처리 상태 저장 |
| 신규 `OutboxEventRepository` | 처리 대상 조회 및 잠금 |
| 신규 이벤트 작업자 | 채팅방·알림 처리와 재시도 |
| `ChatRoomService` | 멱등한 `find-or-create` 제공 |
| `NotificationService` | 이벤트 키 기반 중복 방지 |
| DB 마이그레이션 | Outbox 테이블과 인덱스 추가 |
| 모니터링 | 대기·실패 이벤트 수와 처리 시간 수집 |

## 9. 테스트 계획

### 9.1 주 트랜잭션 롤백

- Payment 변경 후 Booking 변경 실패
- Booking 변경 후 Outbox 저장 실패
- 만료 처리 중 Slot 반환 실패

기대 결과는 관련 DB 변경 전체 롤백이다.

### 9.2 외부 작업 분리

- DB 커밋 전에 FCM이 호출되지 않는지 확인
- DB 롤백 시 Outbox 이벤트가 남지 않는지 확인
- 정상 커밋 후 이벤트 작업자가 후속 작업을 수행하는지 확인

### 9.3 재시도

- FCM 첫 호출 실패 후 다음 재시도 성공
- 채팅방 생성 직후 프로세스 중단 후 재실행
- 최대 재시도 횟수 초과 시 `FAILED` 전환
- 잘못된 payload가 무한 재시도되지 않는지 확인

### 9.4 다중 서버

- 두 작업자가 동시에 같은 이벤트를 조회
- 각 이벤트가 한 작업자에게만 할당되는지 확인
- 작업자 중단 후 잠긴 이벤트가 다시 처리 가능한지 확인

### 9.5 PG 장애

- PG 타임아웃 후 조회로 성공 여부 확인
- PG 성공 후 서버 응답 유실
- PG 실패 시 내부 결제가 성공 상태로 변경되지 않는지 확인

## 10. 구현 순서

1. 핵심 DB 상태 변경과 외부 작업 분리
2. Outbox 테이블과 이벤트 작업자 구현
3. 알림 및 채팅방 후속 처리 재시도 구현
4. 장애 및 롤백 테스트 추가
5. Outbox 지연 및 실패 모니터링 추가

## 11. 완료 조건

- 결제와 예약의 핵심 상태 변경이 하나의 짧은 DB 트랜잭션에서 처리된다.
- DB 트랜잭션 안에서 FCM이나 긴 외부 API 호출을 기다리지 않는다.
- DB 롤백 시 결제 완료 알림이 발송되지 않는다.
- 정상 커밋 시 Outbox 이벤트가 반드시 존재한다.
- 이벤트 재처리 시 채팅방과 알림이 중복 생성되지 않는다.
- 일시적인 외부 오류는 설정된 정책에 따라 재시도된다.
- 최대 재시도 실패 이벤트를 운영자가 확인할 수 있다.
- 다중 서버에서도 같은 이벤트를 중복 처리하지 않는다.
- 장애 및 롤백 통합 테스트가 통과한다.
