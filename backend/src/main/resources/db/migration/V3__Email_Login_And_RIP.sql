-- Add RIP to profiles and migrate users.username to store email (email becomes the authenticated identifier)

ALTER TABLE profiles
    ADD COLUMN rip VARCHAR(50);

-- Ensure existing rows have a value (temporary) to allow unique constraint addition later.
-- Users will be required to set a real RIP through registration or admin update.
UPDATE profiles
SET rip = CONCAT('TEMP-', user_id)
WHERE rip IS NULL;

ALTER TABLE profiles
    ADD CONSTRAINT uk_profiles_rip UNIQUE (rip);

CREATE INDEX idx_profiles_rip ON profiles(rip);

-- Store email in users.username so existing auth/JWT subject mechanisms work with email.
UPDATE users u
SET username = LOWER(p.email)
FROM profiles p
WHERE p.user_id = u.id;
