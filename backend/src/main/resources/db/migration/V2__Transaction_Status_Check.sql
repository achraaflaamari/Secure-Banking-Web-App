-- Align DB transaction status constraint with application statuses
-- Some environments already have a CHECK constraint generated externally (e.g., earlier schema versions)
-- We ensure it exists and allows the statuses used by the application.

ALTER TABLE transactions
    DROP CONSTRAINT IF EXISTS transactions_status_check;

ALTER TABLE transactions
    ADD CONSTRAINT transactions_status_check
    CHECK (status IN ('PENDING', 'COMPLETED', 'REVERSED'));
