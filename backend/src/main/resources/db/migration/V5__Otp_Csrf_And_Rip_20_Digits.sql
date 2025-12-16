-- Add email OTP table (used for signup verification, login 2FA, and password reset)

CREATE TABLE IF NOT EXISTS email_otps (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    purpose VARCHAR(30) NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_email_otps_email_purpose ON email_otps(email, purpose);

-- Enforce RIP format to be 20 digits (existing rows are normalized)

ALTER TABLE profiles
    ALTER COLUMN rip TYPE VARCHAR(20);

-- Replace TEMP-* RIP values with a 20-digit pseudo-random value (best-effort)
UPDATE profiles
SET rip = (
    LPAD(FLOOR(RANDOM() * 10000000000)::bigint::text, 10, '0') ||
    LPAD(FLOOR(RANDOM() * 10000000000)::bigint::text, 10, '0')
)
WHERE rip IS NULL OR rip LIKE 'TEMP-%' OR rip !~ '^[0-9]{20}$';

-- Add check constraint
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_profiles_rip_20_digits'
    ) THEN
        ALTER TABLE profiles
            ADD CONSTRAINT ck_profiles_rip_20_digits CHECK (rip ~ '^[0-9]{20}$');
    END IF;
END$$;
