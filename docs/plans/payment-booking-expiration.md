# 예약 및 결제 만료 처리 계획

## 1. 문서 목적

미결제 예약과 결제가 정해진 시간에 함께 만료되고, 예약 슬롯이 안전하게 반환되도록 개선한다.

이 단계는 [1단계 동시성 제어](./payment-booking-concurrency-control.md)와 [2단계 멱등성 보장](./payment-booking-idempotency.md)이 완료된 후 진행한다.

## 2. 현재 문제

### 2.1 만료 작업 비활성화

`BookingService.cancelExpiredBookings()`의 `@Scheduled`가 주석 처리되어 있다. 따라서 미결제 상태가 계속 남을 수 있다.

```text
Payment = AWAITING_DEPOSIT
Booking = PENDING
Slot = BOOKED
```

`Payment.expire()`와 만료 결제 조회 메서드도 실제 만료 흐름에서 사용되지 않는다.

### 2.2 만료 기준 불일치

현재 예약과 결제의 시간 기준이 다르다.

```text
예약 결제 대기 시간 = 10분
무통장 입금 기한 = 24시간
```

스케줄러만 활성화하면 고객에게는 24시간을 안내하지만 예약은 10분 뒤 취소될 수 있다.

### 2.3 만료와 입금 신고의 충돌

입금 신고와 만료 작업이 동시에 실행되면 다음과 같은 잘못된 상태가 생길 수 있다.

```text
Payment = DEPOSIT_REPORTED
Booking = CANCELLED
Slot = AVAILABLE
```

## 3. 상태 정책

### 3.1 하나의 만료 시각 사용

결제 생성 시 하나의 `expiresAt`을 결정하고 예약과 결제가 같은 값을 사용한다.

```text
Payment.expiresAt = 2026-08-13T12:00:00Z
Booking.expiresAt = 2026-08-13T12:00:00Z
```

서버의 기준 시간은 UTC를 사용한다.

### 3.2 만료 대상

권장 대상 조건:

```text
Payment.status = AWAITING_DEPOSIT
AND Payment.expiresAt <= 현재 UTC 시각
```

`DEPOSIT_REPORTED` 상태는 자동 만료하지 않고 관리자 확인 대상으로 유지하는 것을 권장한다.

### 3.3 상태 전이

```text
Payment: AWAITING_DEPOSIT → EXPIRED
Booking: PENDING → EXPIRED
Slot: BOOKED → AVAILABLE
```

사용자 취소와 시스템 만료를 구분하려면 `BookingStatus.EXPIRED`를 추가한다.

## 4. 구현 계획

### 4.1 만료 시각 통합

- 결제 설정에 하나의 대기 시간을 정의한다.
- 예약 및 결제 생성 시 같은 `expiresAt`을 저장한다.
- 기존 10분 상수와 24시간 설정 중 하나만 남긴다.
- API 응답도 저장된 `expiresAt`을 반환한다.

### 4.2 만료 상태 전이를 한 트랜잭션으로 처리

한 만료 건은 다음 순서로 처리한다.

```text
Payment 잠금
→ Booking 잠금
→ Slot 잠금 또는 버전 확인
→ 현재 상태와 expiresAt 재검증
→ Payment 만료
→ Booking 만료
→ Slot 반환
→ 커밋
```

중간 작업이 실패하면 모든 상태 변경이 롤백되어야 한다.

### 4.3 만료 대상 조회

한 번에 모든 만료 건을 읽지 않고 일정한 크기로 나누어 처리한다.

```text
status = AWAITING_DEPOSIT
expires_at <= now
ORDER BY expires_at, id
LIMIT 100
```

인덱스 예시:

```sql
CREATE INDEX idx_payment_expiration
ON payment(status, expires_at);
```

### 4.4 안전한 스케줄러 활성화

- 고정 주기는 설정 파일로 관리한다.
- 한 건의 실패가 전체 작업을 중단하지 않게 한다.
- 처리 건수와 실패 건수를 로그 및 메트릭으로 기록한다.
- 동일 작업 재실행 시 이미 만료된 건은 건너뛴다.

### 4.5 다중 서버 중복 실행 방지

서버가 여러 대라면 각 서버가 같은 만료 건을 조회할 수 있다.

PostgreSQL에서는 다음 방식을 권장한다.

```text
SELECT ...
FOR UPDATE SKIP LOCKED
```

각 서버는 다른 행을 가져가 처리한다. 잠긴 행을 기다리지 않으므로 작업 분배가 가능하다.

대안:

- ShedLock과 같은 분산 스케줄러 잠금
- 스케줄러 전용 인스턴스
- 메시지 큐 기반 지연 작업

현재 규모에서는 DB 행 잠금 또는 ShedLock 중 하나로 시작할 수 있다.

### 4.6 만료와 입금 신고의 경쟁 처리

입금 신고와 만료 작업 모두 `Payment → Booking → Slot` 순서로 잠근다.

잠금을 얻은 후 상태와 시각을 다시 검사한다.

```text
입금 신고가 먼저 완료
→ Payment = DEPOSIT_REPORTED
→ 만료 작업은 처리 대상에서 제외

만료가 먼저 완료
→ Payment = EXPIRED
→ 입금 신고는 만료 오류 반환
```

## 5. 구현 대상 파일

| 대상 | 변경 내용 |
|---|---|
| `Payment` | `expiresAt` 사용 및 만료 상태 전이 보완 |
| `Booking` | `expiresAt`, `EXPIRED` 상태 추가 검토 |
| `PaymentService` | 잠금 기반 만료 처리 메서드 추가 |
| 만료 작업 클래스 | 대상 조회와 개별 처리 분리 |
| `PaymentRepository` | 만료 대상 배치 조회 및 잠금 쿼리 추가 |
| 설정 파일 | 만료 시간과 스케줄 주기 통합 |
| DB 마이그레이션 | 컬럼과 인덱스 추가 |

## 6. 테스트 계획

### 6.1 기본 만료

- 만료 시각이 지난 결제만 처리되는지 확인
- Payment, Booking, Slot이 함께 변경되는지 확인
- 아직 만료되지 않은 결제는 유지되는지 확인
- `PAID`, `DEPOSIT_REPORTED` 결제는 자동 만료되지 않는지 확인

### 6.2 경계 시각

- `expiresAt` 직전에는 만료되지 않는지 확인
- `expiresAt`과 같은 시각에는 만료되는지 확인
- 모든 비교가 UTC 기준인지 확인

### 6.3 동시 실행

- 입금 신고와 만료를 동시에 실행
- 결제 승인과 만료를 동시에 실행
- 두 서버가 같은 만료 건을 동시에 처리
- 만료 작업을 반복 실행

### 6.4 실패 및 롤백

- Slot 반환 실패 시 Payment와 Booking 변경도 롤백되는지 확인
- 한 결제의 만료 실패가 다음 결제 처리를 막지 않는지 확인
- 서버 중단 후 다음 실행에서 미처리 건이 다시 처리되는지 확인

## 7. 구현 순서

1. 예약과 결제의 만료 시간을 하나로 통합
2. 만료 상태 전이를 하나의 트랜잭션으로 구현
3. 안전한 스케줄러 활성화
4. 다중 서버 중복 실행 방지
5. 경계 시각·동시성·롤백 테스트 추가

## 8. 완료 조건

- 예약과 결제가 같은 `expiresAt`을 사용한다.
- 만료 시 Payment, Booking, Slot이 함께 변경된다.
- 입금 신고 또는 승인된 결제가 잘못 만료되지 않는다.
- 만료 작업을 반복해도 결과가 변하지 않는다.
- 여러 서버가 동시에 작업해도 각 결제는 한 번만 처리된다.
- 만료 처리 실패 시 관련 상태가 모두 롤백된다.
- PostgreSQL 기반 만료 통합 테스트가 통과한다.
