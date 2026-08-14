# Payment 및 Booking 버전 필드 적용 계획

## 1. 목적

`Payment`와 `Booking`에 JPA 낙관적 락 버전을 추가해, 같은 데이터를 동시에 변경할 때 
나중 요청이 앞선 요청의 결과를 조용히 덮어쓰지 못하게 한다.

이 문서는 [동시성 제어 문서의 3.2](./payment-booking-concurrency-control.md#32-payment와-booking에는-버전-필드가-없음)를 구현하기 위한 세부 계획이다.

## 2. 작업 범위

이번 작업에 포함한다.

1. `Payment.version` 추가
2. `Booking.version` 추가
3. 기존 DB 데이터의 버전 초기값 적용
4. 낙관적 락 충돌을 `409 Conflict`로 변환
5. 엔티티 및 통합 테스트 추가

이번 작업에서는 다음 내용을 구현하지 않는다.

- 비관적 락 조회
- 잠금 순서 통일
- 승인 요청 멱등키
- 만료 스케줄러
- Outbox

위 내용은 다음 단계에서 별도로 처리한다.

## 3. 현재 상태

### 3.1 이미 적용된 엔티티

`CounselorAvailability`에는 다음 버전 필드가 있다.

```java
@Version
private Integer version;
```

### 3.2 적용되지 않은 엔티티

- `payment/entity/Payment.java`
- `livechat/entity/Booking.java`

두 엔티티는 동시에 수정되어도 버전 충돌을 감지하지 못한다.

### 3.3 현재 예외 응답

`ControllerAdviceException`에는 낙관적 락 전용 처리기가 없다. 따라서 `ObjectOptimisticLockingFailureException`이 발생하면 일반 `Exception` 처리기에 의해 `500 Internal Server Error`로 응답할 가능성이 있다.

목표 응답은 `409 Conflict`이다.

## 4. 구현 순서

### 4.1 `Payment`에 버전 필드 추가

대상 파일:

```text
src/main/java/com/example/kbuddy_backend/payment/entity/Payment.java
```

변경 예시:

```java
import jakarta.persistence.Version;

@Version
@Column(nullable = false)
private Long version;
```

버전 값은 애플리케이션 코드에서 직접 변경하지 않는다. JPA가 엔티티 변경 시 자동으로 증가시킨다.

### 4.2 `Booking`에 버전 필드 추가

대상 파일:

```text
src/main/java/com/example/kbuddy_backend/livechat/entity/Booking.java
```

변경 예시:

```java
import jakarta.persistence.Version;

@Version
@Column(nullable = false)
private Long version;
```

### 4.3 데이터베이스 컬럼 추가

필요한 변경:

```sql
ALTER TABLE payment
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE booking
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
```

기존 데이터가 있으므로 컬럼을 바로 `NOT NULL`로 추가할 때 기본값 또는 데이터 보정 과정이 필요하다.

더 안전한 운영 적용 순서:

```sql
ALTER TABLE payment ADD COLUMN version BIGINT;
UPDATE payment SET version = 0 WHERE version IS NULL;
ALTER TABLE payment ALTER COLUMN version SET NOT NULL;

ALTER TABLE booking ADD COLUMN version BIGINT;
UPDATE booking SET version = 0 WHERE version IS NULL;
ALTER TABLE booking ALTER COLUMN version SET NOT NULL;
```

현재 프로젝트에는 Flyway가 없고 `ddl-auto: update`를 사용한다. 개발 환경에서는 Hibernate가 컬럼을 만들 수 있지만, 운영 배포에서는 자동 변경에만 의존하지 않고 SQL 적용 여부를 확인해야 한다.

### 4.4 낙관적 락 예외를 409로 변환

대상 파일:

```text
src/main/java/com/example/kbuddy_backend/common/advice/ControllerAdviceException.java
```

처리 대상:

```java
ObjectOptimisticLockingFailureException
```

권장 응답:

```http
HTTP/1.1 409 Conflict
```

현재 `ErrorResponse` 형식에 맞춘 예시:

```java
@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
public ResponseEntity<ErrorResponse> handleOptimisticLock(
        ObjectOptimisticLockingFailureException exception) {
    log.warn("Optimistic lock conflict", exception);
    return ResponseEntity.status(CONFLICT)
            .body(new ErrorResponse(
                    "다른 요청이 먼저 처리되었습니다. 최신 상태를 확인해주세요.",
                    CustomCode.HTTP_409));
}
```

응답에 내부 엔티티 정보나 스택 트레이스를 포함하지 않는다.

### 4.5 서비스 내부 충돌 예외 처리 제거 또는 정리

`BookingService.reserve()`는 메서드 내부에서 `ObjectOptimisticLockingFailureException`을 잡고 있다.

낙관적 락 예외는 트랜잭션 커밋 시점에 발생할 수 있어 메서드 내부 `try-catch`가 잡지 못할 수 있다. 전역 예외 처리기에서 409로 변환하는 방식을 기본으로 한다.

선택 사항:

- 기존 `try-catch`를 제거하고 전역 처리기로 통일
- 슬롯 충돌 메시지를 구분해야 한다면 별도의 도메인 예외로 변환하는 트랜잭션 경계 설계 추가

이번 작업에서는 전역 409 처리를 먼저 적용하고, 슬롯 전용 메시지 분리는 후속 작업으로 남긴다.

## 5. 테스트 계획

### 5.1 엔티티 매핑 테스트

확인 항목:

- 새 `Payment` 저장 시 `version`이 생성되는지 확인
- 새 `Booking` 저장 시 `version`이 생성되는지 확인
- 엔티티 변경 후 버전이 증가하는지 확인

Mockito 단위 테스트만으로 JPA 버전 증가를 검증할 수 없다. 실제 JPA 저장과 플러시가 필요한 테스트를 사용한다.

### 5.2 `Payment` 충돌 통합 테스트

테스트 흐름:

```text
1. 같은 Payment를 두 개의 트랜잭션에서 조회
2. 트랜잭션 A가 상태를 변경하고 커밋
3. 트랜잭션 B가 이전 버전으로 상태를 변경하고 커밋
4. 트랜잭션 B에서 낙관적 락 예외 확인
5. DB 최종 상태가 트랜잭션 A의 결과인지 확인
```

### 5.3 `Booking` 충돌 통합 테스트

테스트 흐름:

```text
1. 같은 Booking을 두 개의 트랜잭션에서 조회
2. 트랜잭션 A가 상태를 변경하고 커밋
3. 트랜잭션 B가 이전 버전으로 상태를 변경하고 커밋
4. 트랜잭션 B에서 낙관적 락 예외 확인
5. DB 최종 상태가 트랜잭션 A의 결과인지 확인
```

### 5.4 API 예외 응답 테스트

확인 항목:

- `ObjectOptimisticLockingFailureException`이 `409 Conflict`로 변환되는지 확인
- 응답 메시지가 사용자에게 최신 상태 재조회를 안내하는지 확인
- 스택 트레이스가 응답에 포함되지 않는지 확인

### 5.5 테스트 DB

최종 동시성 테스트는 운영 환경과 같은 PostgreSQL에서 실행하는 것이 좋다. 첫 구현에서는 기존 테스트 환경으로 매핑과 예외 응답을 확인하고, 실제 병렬 충돌 테스트는 PostgreSQL Testcontainers 도입 시 추가할 수 있다.

## 6. 예상 변경 파일

| 파일 | 변경 내용 |
|---|---|
| `payment/entity/Payment.java` | `Long version` 추가 |
| `livechat/entity/Booking.java` | `Long version` 추가 |
| `common/advice/ControllerAdviceException.java` | 낙관적 락 409 처리 추가 |
| DB 변경 파일 또는 운영 SQL | 두 테이블의 `version` 컬럼 추가 |
| JPA 통합 테스트 | 버전 증가 및 충돌 검증 |
| 예외 처리 테스트 | 409 응답 검증 |

## 7. 구현 시 주의사항

### 7.1 버전을 API 요청으로 받지 않음

현재 목표는 서버 내부의 동시 상태 변경 충돌을 감지하는 것이다. DTO에 `version`을 추가해 클라이언트가 임의로 보내게 하지 않는다.

### 7.2 충돌 트랜잭션에서 재저장하지 않음

낙관적 락 예외가 발생한 트랜잭션은 롤백한다. 같은 트랜잭션에서 최신 데이터를 다시 조회해 저장하려고 하면 안 된다.

### 7.3 자동 재시도하지 않음

결제 승인, 취소, 입금 신고는 충돌 시 자동으로 반복하지 않는다. `409 Conflict`를 반환하고 클라이언트가 최신 상태를 조회하게 한다.

자동 재시도는 만료 배치처럼 상태를 다시 확인한 후 안전하게 반복할 수 있는 내부 작업에서만 별도로 검토한다.

### 7.4 `@Version`만으로 전체 동시성 문제는 해결되지 않음

`@Version`은 충돌을 감지하지만 결제 승인 요청을 직렬화하지는 않는다. 다음 단계에서 결제 승인 조회에 비관적 락을 적용하고 `Payment → Booking → Slot` 잠금 순서를 통일해야 한다.

## 8. 완료 조건

- `Payment`와 `Booking`에 `@Version` 필드가 존재한다.
- 기존 DB 행의 `version` 값이 모두 0 이상이며 `NULL`이 아니다.
- 엔티티를 변경할 때 버전이 증가한다.
- 오래된 버전으로 저장하면 낙관적 락 예외가 발생한다.
- 충돌 요청은 `500`이 아니라 `409 Conflict`를 받는다.
- 충돌한 트랜잭션의 일부 변경이 DB에 남지 않는다.
- 기존 결제 및 예약 단위 테스트가 모두 통과한다.
- 비관적 락과 잠금 순서 통일은 후속 단계로 명확히 남아 있다.
