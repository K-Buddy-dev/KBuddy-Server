-- Apply this migration before deploying the application version that adds
-- @Version to the payment and booking entities.

ALTER TABLE payment ADD COLUMN IF NOT EXISTS version BIGINT;
UPDATE payment SET version = 0 WHERE version IS NULL;
ALTER TABLE payment ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE payment ALTER COLUMN version SET NOT NULL;

ALTER TABLE booking ADD COLUMN IF NOT EXISTS version BIGINT;
UPDATE booking SET version = 0 WHERE version IS NULL;
ALTER TABLE booking ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE booking ALTER COLUMN version SET NOT NULL;
