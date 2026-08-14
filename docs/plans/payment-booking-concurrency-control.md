# 결제 및 예약 동시성 제어 개선 계획

## 1. 문서 목적

현재 `Feat/paymentAdv` 브랜치의 결제 및 예약 기능에서 발생할 수 있는 동시성 문제를 분석하고, 
실제 구현에 사용할 잠금 방식과 테스트 방법을 정리한다.

이 문서는 다음 항목만 다룬다.

- 동일 슬롯 동시 예약
- 결제 승인과 입금 신고의 충돌
- 결제 승인과 예약 취소의 충돌
- 동일 결제의 동시 승인
- 잠금 순서와 데드락 방지
- 동시성 예외 처리
- PostgreSQL 기반 동시성 테스트

멱등성, 결제 만료, 트랜잭션 후속 이벤트는 별도 문서에서 다룬다. 다만 동시성 문제를 해결하는 데 직접 필요한 내용은 일부 포함한다.

---

## 2. 핵심 결론

현재 구조에서는 다음과 같이 적용하는 것을 권장한다.

| 대상 | 권장 방식 | 이유 |
|---|---|---|
| `CounselorAvailability` | 기존 `@Version` 유지 | 슬롯 충돌은 자주 발생하지 않으며 현재 구조를 활용할 수 있음 |
| `Payment` | `@Version` 추가 + 상태 변경 시 비관적 락 | 결제 승인, 신고, 취소가 동시에 실행되면 금전 상태가 달라질 수 있음 |
| `Booking` | `@Version` 추가 + 결제 관련 변경 시 비관적 락 | 결제와 예약 상태가 함께 변경되므로 동일 트랜잭션에서 보호해야 함 |
| 채팅방 | `booking_id` 유니크 제약조건 유지 | 애플리케이션 잠금 실패 시 중복 생성을 막는 마지막 방어선 |

모든 결제 관련 상태 변경은 다음 잠금 순서를 사용한다.

```text
Payment
→ Booking
→ CounselorAvailability 목록
```

한 기능이라도 반대 순서로 잠그면 데드락 가능성이 생기므로 순서를 통일해야 한다.

---

## 3. 현재 코드 상태

### 3.1 슬롯에는 낙관적 락이 적용되어 있음

`CounselorAvailability`에는 다음 필드가 있다.

```java
@Version
private Integer version;
```

예약 시 `findAllByIdWithLock()`에 `OPTIMISTIC` 잠금을 사용하고, 슬롯의 상태를 `AVAILABLE`에서 `BOOKED`로 변경한다.

두 요청이 같은 슬롯을 동시에 조회하더라도 먼저 커밋된 요청이 버전을 변경한다. 나중 요청은 커밋 과정에서 낙관적 락 충돌을 감지할 수 있다.

### 3.2 `Payment`와 `Booking`에는 버전 필드가 없음

세부 구현 계획: [Payment 및 Booking 버전 필드 적용 계획](./payment-booking-version-control-plan.md)

현재 `Payment`와 `Booking`에는 `@Version`이 없다. 따라서 동시에 읽은 두 요청이 서로 다른 상태 변경을 수행하면 나중에 반영된 값이 앞선 값을 덮어쓸 수 있다.

영향을 받는 주요 기능은 다음과 같다.

- `PaymentService.reportDeposit()`
- `PaymentService.confirmDeposit()`
- `PaymentService.cancelPayment()`
- `BookingService.cancelBooking()`
- `BookingService.confirmPayment()`
- 예약 만료 처리

#### 버전 충돌 감지 후 처리 원칙

`@Version`을 추가하면 나중에 저장하는 요청에서 `ObjectOptimisticLockingFailureException`이 발생한다. 이 예외가 발생한 트랜잭션은 이미 실패한 상태이므로 같은 트랜잭션 안에서 데이터를 다시 저장하면 안 된다.

처리 순서는 다음과 같다.

```text
버전 충돌 감지
→ 현재 트랜잭션 전체 롤백
→ 요청 종류에 따라 재시도 여부 결정
→ 필요한 경우 새 트랜잭션에서 최신 상태 재조회
→ 최신 상태에 업무 규칙 다시 적용
```

부분적으로 변경된 `Payment`, `Booking`, `Slot` 데이터는 모두 롤백되어야 한다.

#### 사용자 또는 관리자 요청

결제 승인, 입금 신고, 예약 취소와 같은 사용자 요청은 서버에서 자동 재시도하지 않는 것을 기본으로 한다. 최신 상태를 확인하지 않고 상태 변경을 자동 반복하면 먼저 처리된 승인이나 취소를 잘못 덮어쓸 수 있기 때문이다.

```text
버전 충돌
→ 트랜잭션 롤백
→ 409 Conflict 반환
→ 클라이언트가 결제 또는 예약 최신 정보 조회
→ 사용자가 필요한 작업을 다시 요청
```

권장 응답:

```json
{
  "code": "CONCURRENT_MODIFICATION",
  "message": "다른 요청이 먼저 처리되었습니다. 최신 상태를 확인해주세요.",
  "retryable": false
}
```

2단계 멱등성 보장이 구현된 이후에는 최신 상태가 요청의 목표 상태와 같을 때 기존 성공 결과를 반환할 수 있다.

예:

```text
결제 승인 중 버전 충돌
→ 새 트랜잭션에서 최신 Payment 조회
→ 이미 PAID이고 승인 금액도 같음
→ 기존 승인 성공 결과 반환
```

최신 상태가 `CANCELLED`, `EXPIRED`처럼 요청의 목표와 충돌하면 `409 Conflict`를 반환한다.

#### 스케줄러 또는 내부 작업

예약 만료처럼 사용자의 추가 판단이 필요 없는 내부 작업은 제한적으로 자동 재시도할 수 있다.

```text
버전 충돌
→ 기존 트랜잭션 롤백
→ 새 트랜잭션 시작
→ 최신 상태 재조회
→ 아직 만료 조건을 만족하면 다시 처리
→ 이미 PAID 또는 DEPOSIT_REPORTED이면 처리 종료
```

재시도는 2~3회처럼 작은 횟수로 제한하고, 각 재시도 사이에 짧은 지연을 둘 수 있다. 재시도 한도를 넘으면 실패 로그와 메트릭을 남겨 다음 스케줄 실행에서 다시 처리한다.

재시도 로직은 실패하여 rollback-only 상태가 된 기존 트랜잭션 안에서 실행하면 안 된다. 별도 빈의 `REQUIRES_NEW` 메서드 또는 트랜잭션 템플릿으로 새 트랜잭션을 시작해야 한다.

#### 슬롯 예약 요청

슬롯 예약 충돌은 자동 재시도하지 않는다. 충돌한 시점에는 다른 사용자가 슬롯을 선점했을 가능성이 높기 때문이다.

```text
슬롯 버전 충돌
→ 전체 예약 트랜잭션 롤백
→ 409 SLOT_ALREADY_BOOKED 반환
→ 클라이언트가 예약 가능 슬롯 목록 재조회
```

#### 처리 기준 요약

| 요청 종류 | 자동 재시도 | 충돌 후 처리 |
|---|---|---|
| 슬롯 예약 | 하지 않음 | 롤백 후 `409 SLOT_ALREADY_BOOKED` |
| 결제 승인 | 기본적으로 하지 않음 | 롤백 후 최신 상태 조회 안내 |
| 입금 신고 | 하지 않음 | 롤백 후 `409 CONCURRENT_MODIFICATION` |
| 예약·결제 취소 | 하지 않음 | 롤백 후 최신 상태 조회 안내 |
| 예약·결제 만료 작업 | 제한적으로 수행 | 새 트랜잭션에서 재조회·재검증 후 재시도 |

### 3.3 현재 예약 충돌 예외 처리가 동작하지 않을 가능성

`BookingService.reserve()` 내부에서 `ObjectOptimisticLockingFailureException`을 잡고 있지만, 실제 낙관적 락 예외는 트랜잭션 커밋 시점에 발생할 수 있다.

Spring의 `@Transactional` 프록시는 서비스 메서드가 반환된 뒤 커밋한다. 따라서 메서드 내부의 `try-catch`가 커밋 시점 예외를 잡지 못할 수 있다.

현재 형태:

```text
reserve() 메서드 실행 완료
→ 메서드 내부 try-catch 종료
→ Spring 트랜잭션 커밋
→ 낙관적 락 예외 발생
```

그 결과 사용자가 의도한 안내 메시지 대신 공통 500 오류를 받을 수 있다.

---

## 4. 동시성 문제 시나리오

### 4.1 동일 슬롯 동시 예약

#### 상황

사용자 A와 사용자 B가 같은 슬롯을 거의 동시에 예약한다.

```text
요청 A: Slot 10 조회 → AVAILABLE
요청 B: Slot 10 조회 → AVAILABLE
요청 A: BOOKED로 변경
요청 B: BOOKED로 변경
```

#### 현재 방어 수단

- `CounselorAvailability.version`
- `OPTIMISTIC` 잠금
- `BookingSlot.availability_id` 유니크 제약조건

#### 남은 문제

- 충돌 예외가 커밋 시점에 발생하면 서비스 내부에서 변환하지 못할 수 있다.
- 클라이언트가 일관된 `409 Conflict` 응답을 받지 못할 수 있다.
- 슬롯 ID 목록의 중복 입력을 별도로 검증하지 않는다.

#### 목표 결과

```text
한 요청만 예약 성공
다른 요청은 409 Conflict
Booking과 Payment는 성공한 요청에 대해서만 생성
```

---

### 4.2 결제 승인과 입금 신고 동시 실행

#### 상황

```text
요청 A: 관리자가 결제를 승인
요청 B: 고객이 입금 완료를 신고

두 요청의 조회 상태
Payment = AWAITING_DEPOSIT
```

#### 발생 가능한 잘못된 결과

```text
요청 A가 PAID로 변경
요청 B가 나중에 DEPOSIT_REPORTED로 반영

최종 상태 = DEPOSIT_REPORTED
```

이미 완료된 결제가 이전 상태로 돌아간다.

#### 목표 결과

먼저 잠금을 획득한 요청만 상태를 변경한다. 다음 요청은 잠금 해제 후 최신 상태를 조회하고 상태 규칙에 따라 처리한다.

```text
승인이 먼저 완료된 경우
→ 입금 신고 요청은 PAID 상태를 확인하고 거부 또는 기존 결과 반환

입금 신고가 먼저 완료된 경우
→ 승인 요청은 DEPOSIT_REPORTED 상태를 확인한 뒤 정상 승인
```

---

### 4.3 결제 승인과 예약 취소 동시 실행

#### 상황

```text
요청 A: 관리자가 결제를 승인
요청 B: 고객이 예약을 취소

두 요청의 조회 상태
Payment = AWAITING_DEPOSIT
Booking = PENDING
```

#### 발생 가능한 잘못된 결과

```text
Payment = PAID
Booking = CANCELLED
Slot = AVAILABLE
ChatRoom = 생성됨
```

#### 원인

결제 승인은 `Payment`를 먼저 조회하지만, 예약 취소는 `Booking`만 조회한다. 두 기능이 같은 공유 자원을 같은 방식으로 잠그지 않는다.

#### 목표 결과

예약 취소도 연결된 `Payment`를 먼저 잠근 후 `Booking`을 잠근다.

```text
승인이 먼저 잠금을 획득
→ Payment와 Booking을 PAID로 변경
→ 취소 요청은 PAID 상태에 맞는 정책으로 처리

취소가 먼저 잠금을 획득
→ Payment와 Booking을 CANCELLED로 변경
→ 승인 요청은 취소 상태를 확인하고 거부
```

---

### 4.4 동일 결제 동시 승인

#### 상황

두 관리자가 같은 결제의 승인 버튼을 거의 동시에 누른다.

#### 발생 가능한 문제

- 두 요청 모두 결제를 `PAID`로 변경하려고 함
- 두 요청 모두 예약을 `PAID`로 변경하려고 함
- 채팅방 중복 생성 시도
- 알림 중복 생성 및 발송
- `ChatRoom.bookingId` 유니크 제약조건 오류

#### 목표 결과

```text
첫 번째 요청: 승인 처리 성공
두 번째 요청: 잠금 해제 후 PAID 상태 확인
→ 상태를 다시 변경하지 않음
→ 후속 작업을 다시 생성하지 않음
```

두 번째 요청에 기존 성공 결과를 반환할지는 멱등성 정책에서 최종 결정한다.

---

## 5. 권장 설계

### 5.1 `Payment`와 `Booking`에 `@Version` 추가

두 엔티티에 버전 필드를 추가한다.

```java
@Version
@Column(nullable = false)
private Long version;
```

#### 목적

- 잠금이 적용되지 않은 코드 경로의 변경 충돌 감지
- 잘못된 마지막 쓰기 방지
- 운영 중 새로운 상태 변경 기능이 추가될 때 최소한의 안전장치 제공

#### 주의사항

- 기존 데이터가 있다면 마이그레이션에서 `version = 0`으로 채워야 한다.
- 운영 DB는 Hibernate 자동 변경보다 Flyway 마이그레이션을 사용하는 것이 안전하다.

예시 SQL:

```sql
ALTER TABLE payment ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE booking ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
```

### 5.2 상태 변경 조회용 비관적 락 추가

#### PaymentRepository

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select p from Payment p where p.id = :paymentId")
Optional<Payment> findByIdForUpdate(@Param("paymentId") Long paymentId);

@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select p from Payment p where p.booking.id = :bookingId")
Optional<Payment> findByBookingIdForUpdate(@Param("bookingId") Long bookingId);
```

#### BookingRepository

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select b from Booking b where b.id = :bookingId")
Optional<Booking> findByIdForUpdate(@Param("bookingId") Long bookingId);
```

#### 적용 대상

- 결제 승인
- 입금 신고
- 결제 취소
- 예약 취소
- 예약 및 결제 만료
- 향후 환불 처리

단순 조회 API에는 비관적 락을 사용하지 않는다.

### 5.3 잠금 전용 서비스 흐름

결제 관련 상태 변경은 `Payment`를 동시성 제어의 시작점으로 사용한다.

예시 승인 흐름:

```java
@Transactional
public PaymentResponse confirmDeposit(Long paymentId, PaymentConfirmRequest request) {
    Payment payment = paymentRepository.findByIdForUpdate(paymentId)
            .orElseThrow(() -> new NotFoundException("결제를 찾을 수 없습니다"));

    Booking booking = bookingRepository.findByIdForUpdate(payment.getBooking().getId())
            .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다"));

    validateConfirmable(payment, booking);
    payment.confirmDeposit(request.confirmedAmount(), request.adminMemo());
    booking.confirmPayment();

    return PaymentResponse.from(payment);
}
```

예시 예약 취소 흐름:

```java
@Transactional
public void cancelBooking(User user, Long bookingId) {
    Payment payment = paymentRepository.findByBookingIdForUpdate(bookingId)
            .orElseThrow(() -> new NotFoundException("결제를 찾을 수 없습니다"));

    Booking booking = bookingRepository.findByIdForUpdate(bookingId)
            .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다"));

    validateCancellation(user, payment, booking);
    payment.cancel("고객 예약 취소");
    booking.cancel("고객 예약 취소");
}
```

실제 취소 가능 상태와 환불 정책은 별도로 결정해야 한다. 여기서 중요한 점은 모든 기능이 `Payment → Booking` 순서로 잠금을 얻는 것이다.

### 5.4 슬롯 목록 처리 순서 고정

여러 슬롯을 함께 처리할 때는 슬롯 ID를 정렬한 뒤 조회한다.

```text
[15, 10, 12]
→ [10, 12, 15]
→ 같은 순서로 처리
```

현재는 낙관적 락을 사용하므로 DB 행 잠금 순서의 영향이 크지 않다. 그러나 향후 슬롯에 비관적 락을 적용하거나 만료·취소 과정에서 여러 슬롯을 잠글 경우를 대비해 처리 순서를 통일하는 것이 좋다.

요청의 중복 슬롯 ID도 제거하지 말고 오류로 처리해야 한다.

```text
요청 slotIds = [10, 10]
→ 400 Bad Request
```

### 5.5 잠금 시간 제한

비관적 락은 무기한 기다리지 않도록 제한 시간을 설정한다.

예시:

```java
@QueryHints(@QueryHint(
        name = "jakarta.persistence.lock.timeout",
        value = "3000"
))
```

제한 시간은 운영 부하 테스트 결과에 따라 조정한다. 잠금 시간 초과는 `409 Conflict` 또는 `423 Locked` 중 프로젝트의 API 규칙에 맞는 응답으로 변환한다.

### 5.6 DB 제약조건 유지

애플리케이션 잠금만 신뢰하지 않고 다음 유니크 제약조건을 유지한다.

- `payment.booking_id`
- `chat_room.booking_id`
- `counselor_availability(counselor_id, slot_start_utc)`

DB 제약조건은 동시성 제어가 누락된 코드가 추가되더라도 중복 데이터 저장을 막는 마지막 방어선이다.

---

## 6. 동시성 예외 처리

### 6.1 변환할 예외

다음 예외를 공통 예외 처리기에서 일관되게 변환한다.

- `ObjectOptimisticLockingFailureException`
- `OptimisticLockException`
- `PessimisticLockException`
- `CannotAcquireLockException`
- `DataIntegrityViolationException`

`DataIntegrityViolationException`은 모든 경우를 동시성 오류로 처리하면 안 된다. 제약조건 이름이나 원인을 확인해 예약·결제 중복인 경우에만 적절한 응답으로 변환한다.

### 6.2 권장 HTTP 응답

```http
HTTP/1.1 409 Conflict
Content-Type: application/json

{
  "code": "CONCURRENT_MODIFICATION",
  "message": "다른 요청이 먼저 처리되었습니다. 최신 상태를 확인해주세요."
}
```

슬롯 예약 충돌은 별도 코드로 구분할 수 있다.

```json
{
  "code": "SLOT_ALREADY_BOOKED",
  "message": "다른 사용자가 먼저 예약한 시간입니다."
}
```

### 6.3 커밋 시점 예외 처리

`reserve()` 메서드 내부의 `try-catch`에만 의존하지 않는다. 다음 중 하나를 적용한다.

#### 권장 방법

`@RestControllerAdvice`에서 `ObjectOptimisticLockingFailureException`을 처리한다. 트랜잭션 프록시의 커밋 과정에서 발생한 예외도 처리할 수 있다.

#### 필요한 경우

서비스 안에서 반드시 예외를 확인해야 한다면 `saveAndFlush()` 또는 `EntityManager.flush()`로 플러시 시점을 앞당길 수 있다. 다만 불필요한 플러시는 성능에 영향을 줄 수 있으므로 공통 예외 처리를 우선한다.

---

## 7. 구현 대상 파일

| 파일 | 변경 내용 |
|---|---|
| `payment/entity/Payment.java` | `@Version` 필드 추가 |
| `livechat/entity/Booking.java` | `@Version` 필드 추가 |
| `payment/repository/PaymentRepository.java` | 결제 ID 및 예약 ID 기반 비관적 락 조회 추가 |
| `livechat/repository/BookingRepository.java` | 예약 ID 기반 비관적 락 조회 추가 |
| `payment/service/PaymentService.java` | 승인·신고·취소 시 잠금 조회 사용 |
| `livechat/service/BookingService.java` | 예약 취소 시 Payment부터 잠그도록 변경 |
| 공통 예외 처리기 | 낙관적·비관적 락 예외를 409 응답으로 변환 |
| DB 마이그레이션 | `payment.version`, `booking.version` 컬럼 추가 |
| 동시성 통합 테스트 | PostgreSQL 기반 병렬 요청 테스트 추가 |

---

## 8. 테스트 전략

### 8.1 테스트 환경

동시성 테스트는 H2가 아니라 PostgreSQL Testcontainers를 사용한다. DB마다 잠금 동작과 격리 수준이 다르기 때문이다.

테스트 조건:

- 각 스레드는 별도의 트랜잭션 사용
- `CountDownLatch` 또는 `CyclicBarrier`로 실행 시점 통제
- 테스트 종료 후 DB의 최종 상태 직접 확인
- API 응답뿐 아니라 생성된 `Payment`, `Booking`, `ChatRoom`, `Notification` 개수 확인

### 8.2 동일 슬롯 동시 예약

#### 실행

같은 슬롯에 대해 두 스레드가 동시에 `reserve()`를 호출한다.

#### 기대 결과

- 성공 1건
- 충돌 1건
- Booking 1건
- Payment 1건
- 슬롯 상태 `BOOKED`
- 슬롯 버전 1회 증가

### 8.3 동일 결제 동시 승인

#### 실행

같은 결제에 대해 두 스레드가 동시에 `confirmDeposit()`을 호출한다.

#### 기대 결과

- 결제 최종 상태 `PAID`
- 예약 최종 상태 `PAID`
- 상태 전환 1회
- 채팅방 최대 1개
- 중복 알림 없음
- 잠금 또는 최신 상태 확인으로 두 번째 요청이 안전하게 종료

### 8.4 결제 승인과 입금 신고 충돌

#### 실행

한 스레드는 `confirmDeposit()`, 다른 스레드는 `reportDeposit()`을 호출한다.

#### 기대 결과

- 최종 결제 상태가 `PAID`에서 `DEPOSIT_REPORTED`로 역행하지 않음
- 두 처리 순서 중 허용된 상태 전이만 발생
- 일부 필드만 변경되는 현상 없음

### 8.5 결제 승인과 예약 취소 충돌

#### 실행

한 스레드는 결제를 승인하고 다른 스레드는 같은 예약을 취소한다.

#### 기대 결과

허용 가능한 최종 상태는 업무 정책으로 정한 한 가지 조합이어야 한다.

예:

```text
승인이 먼저 처리됨
Payment = PAID
Booking = PAID

취소가 먼저 처리됨
Payment = CANCELLED
Booking = CANCELLED
```

다음 조합은 절대 허용하지 않는다.

```text
Payment = PAID
Booking = CANCELLED
Slot = AVAILABLE
```

### 8.6 잠금 시간 초과

#### 실행

첫 번째 트랜잭션이 결제 잠금을 유지한 상태에서 두 번째 요청을 실행한다.

#### 기대 결과

- 설정한 제한 시간 이후 종료
- 500이 아닌 정의된 충돌 응답 반환
- 데이터 변경 없음
- DB 연결이 계속 점유되지 않음

### 8.7 반복 실행

동시성 테스트는 한 번만 실행하면 간헐적 문제를 놓칠 수 있다. CI에서는 주요 테스트를 여러 번 반복하거나 별도 안정성 테스트 작업으로 실행한다.

---

## 9. 구현 순서

### 1단계: 안전장치 추가

1. `Payment`, `Booking`에 `@Version` 추가
2. DB 마이그레이션 추가
3. 동시성 예외 공통 응답 추가
4. 현재 슬롯 예약 충돌 테스트 작성

### 2단계: 결제 잠금 적용

1. `PaymentRepository`에 비관적 락 조회 추가
2. `BookingRepository`에 비관적 락 조회 추가
3. `confirmDeposit()`에 `Payment → Booking` 잠금 적용
4. `reportDeposit()`에 Payment 잠금 적용
5. `cancelPayment()`에 `Payment → Booking` 잠금 적용

### 3단계: 예약 상태 변경 통합

1. `cancelBooking()`이 Payment를 먼저 잠그도록 변경
2. 결제와 예약의 허용 상태 조합 검증 추가
3. 슬롯 목록을 ID 순서로 처리
4. 중복 슬롯 ID 입력 검증 추가

### 4단계: 동시성 통합 테스트

1. PostgreSQL Testcontainers 구성
2. 동일 슬롯 동시 예약 테스트
3. 동일 결제 동시 승인 테스트
4. 승인과 신고 충돌 테스트
5. 승인과 취소 충돌 테스트
6. 잠금 시간 초과 테스트

---

## 10. 완료 조건

- 동일 슬롯의 동시 예약은 한 건만 성공한다.
- 동시성 충돌은 500이 아닌 정의된 409 응답으로 반환된다.
- `Payment`와 `Booking`에 버전 기반 충돌 감지가 적용된다.
- 결제 관련 상태 변경은 항상 `Payment → Booking → Slot` 순서로 잠근다.
- 동일 결제를 동시에 승인해도 상태 변경과 후속 작업이 한 번만 발생한다.
- 결제 승인과 입금 신고가 동시에 실행되어도 상태가 역행하지 않는다.
- 결제 승인과 예약 취소가 동시에 실행되어도 결제와 예약 상태가 서로 다르지 않다.
- 잠금 대기가 제한 시간을 넘으면 안전하게 실패한다.
- PostgreSQL 기반 동시성 통합 테스트가 반복 실행되어도 통과한다.
