ALTER TABLE user
    ADD COLUMN IF NOT EXISTS nickname VARCHAR(50) NULL AFTER email;

UPDATE user
SET nickname = username
WHERE nickname IS NULL OR nickname = '';
