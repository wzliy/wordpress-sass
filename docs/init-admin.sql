START TRANSACTION;

SET @tenant_name = 'Default Tenant';
SET @admin_username = 'admin';
SET @admin_email = 'admin@example.com';
SET @admin_role = 'ADMIN';
SET @admin_password_hash = '$2a$10$8Yk79ao/1W7U.7BcbZRacOThzRbwPRplscgtnY1G2TprM0rDh7exm';

INSERT INTO tenant (name, created_at)
SELECT @tenant_name, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM tenant WHERE name = @tenant_name
);

SET @tenant_id = (
    SELECT id
    FROM tenant
    WHERE name = @tenant_name
    ORDER BY id
    LIMIT 1
);

UPDATE user
SET
    tenant_id = @tenant_id,
    password = @admin_password_hash,
    email = @admin_email,
    nickname = @admin_username,
    role = @admin_role,
    status = 'ACTIVE'
WHERE username = @admin_username;

INSERT INTO user (
    tenant_id,
    username,
    password,
    email,
    nickname,
    role,
    status,
    created_at
)
SELECT
    @tenant_id,
    @admin_username,
    @admin_password_hash,
    @admin_email,
    @admin_username,
    @admin_role,
    'ACTIVE',
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM user WHERE username = @admin_username
);

COMMIT;
