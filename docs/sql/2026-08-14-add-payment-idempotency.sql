-- 멱등키별 요청 내용, 처리 상태, 최초 응답을 저장해 결제 승인의 중복 실행을 방지한다.
CREATE TABLE IF NOT EXISTS payment_idempotency (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(36) NOT NULL,
    operation VARCHAR(30) NOT NULL,
    payment_id BIGINT NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL,
    response_status INTEGER,
    response_body TEXT,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    CONSTRAINT uk_payment_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT fk_payment_idempotency_payment
        FOREIGN KEY (payment_id) REFERENCES payment(id)
);

CREATE INDEX IF NOT EXISTS idx_payment_idempotency_payment
    ON payment_idempotency(payment_id);

-- 승인 후속 알림에 이벤트 키를 부여해 같은 수신자에게 알림이 중복 발송되지 않게 한다.
ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS event_key VARCHAR(100);

CREATE UNIQUE INDEX IF NOT EXISTS uk_notification_event_key
    ON notifications(event_key)
    WHERE event_key IS NOT NULL;
