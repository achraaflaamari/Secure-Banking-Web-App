-- Add permanent deletion flag
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Admin must not have RIP
UPDATE profiles p
SET rip = NULL
FROM users u
WHERE p.user_id = u.id AND u.role = 'ADMIN';
