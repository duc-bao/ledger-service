-- RBAC phase-1 migration.
-- Rollback guidance:
--   1) Revert application code to legacy permission readers.
--   2) Stop writes to the new RBAC tables.
--   3) Drop only the new tables/indexes/constraints introduced here after confirming nothing depends on them.
-- Legacy tables idp_permissions, org_department_permissions and sys_menus.actions are intentionally retained.

ALTER TABLE idp_users
    ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP;

COMMENT ON COLUMN idp_users.last_login_at IS 'Most recent successful login timestamp.';
COMMENT ON COLUMN idp_users.locked_until IS 'Temporary lock expiration; NULL means the account is not temporarily locked.';

ALTER TABLE idp_groups
    ADD COLUMN IF NOT EXISTS status VARCHAR(20),
    ADD COLUMN IF NOT EXISTS sort_order INTEGER;

UPDATE idp_groups
SET code = COALESCE(NULLIF(TRIM(code), ''), 'ROLE_' || UPPER(SUBSTRING(REPLACE(id, '-', '') FROM 1 FOR 20))),
    name = COALESCE(NULLIF(TRIM(name), ''), COALESCE(NULLIF(TRIM(code), ''), 'ROLE_' || UPPER(SUBSTRING(REPLACE(id, '-', '') FROM 1 FOR 20)))),
    status = COALESCE(NULLIF(TRIM(status), ''), 'ACTIVE'),
    sort_order = COALESCE(sort_order, 0);

ALTER TABLE idp_groups
    ALTER COLUMN code SET NOT NULL,
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN sort_order SET NOT NULL,
    ALTER COLUMN status SET DEFAULT 'ACTIVE',
    ALTER COLUMN sort_order SET DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_idp_groups_status ON idp_groups(status);
CREATE INDEX IF NOT EXISTS idx_idp_groups_sort_order ON idp_groups(sort_order);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_idp_groups_status'
    ) THEN
        ALTER TABLE idp_groups
            ADD CONSTRAINT ck_idp_groups_status
            CHECK (status IN ('ACTIVE', 'INACTIVE')) NOT VALID;
    END IF;
END $$;

COMMENT ON TABLE idp_groups IS 'Physical role table retained for compatibility. Business meaning: RBAC role.';
COMMENT ON COLUMN idp_groups.status IS 'Role lifecycle status. ACTIVE roles can be assigned and resolved at runtime.';
COMMENT ON COLUMN idp_groups.sort_order IS 'UI/order hint for role listings.';

DROP INDEX IF EXISTS uq_idp_user_groups_user_id;
DROP INDEX IF EXISTS uk_idp_user_groups_user_id;

ALTER TABLE idp_user_groups
    ADD COLUMN IF NOT EXISTS effective_from TIMESTAMP,
    ADD COLUMN IF NOT EXISTS effective_to TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status VARCHAR(20);

UPDATE idp_user_groups
SET effective_from = COALESCE(effective_from, CURRENT_TIMESTAMP),
    status = COALESCE(NULLIF(TRIM(status), ''), 'ACTIVE');

WITH ranked_duplicates AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY user_id, group_id ORDER BY id) AS rn
    FROM idp_user_groups
)
DELETE FROM idp_user_groups
WHERE id IN (
    SELECT id FROM ranked_duplicates WHERE rn > 1
);

ALTER TABLE idp_user_groups
    ALTER COLUMN group_id SET NOT NULL,
    ALTER COLUMN user_id SET NOT NULL,
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN status SET DEFAULT 'ACTIVE';

CREATE UNIQUE INDEX IF NOT EXISTS uk_idp_user_groups_user_group ON idp_user_groups(user_id, group_id);
CREATE INDEX IF NOT EXISTS idx_idp_user_groups_user_status ON idp_user_groups(user_id, status);
CREATE INDEX IF NOT EXISTS idx_idp_user_groups_group_status ON idp_user_groups(group_id, status);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_idp_user_groups_status'
    ) THEN
        ALTER TABLE idp_user_groups
            ADD CONSTRAINT ck_idp_user_groups_status
            CHECK (status IN ('ACTIVE', 'INACTIVE')) NOT VALID;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_idp_user_groups_effective_range'
    ) THEN
        ALTER TABLE idp_user_groups
            ADD CONSTRAINT ck_idp_user_groups_effective_range
            CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from) NOT VALID;
    END IF;
END $$;

COMMENT ON TABLE idp_user_groups IS 'Global user-role assignments. Multiple active rows per user are supported.';
COMMENT ON COLUMN idp_user_groups.effective_from IS 'Assignment effective start timestamp.';
COMMENT ON COLUMN idp_user_groups.effective_to IS 'Assignment effective end timestamp; NULL means open-ended.';

ALTER TABLE org_department_users
    ADD COLUMN IF NOT EXISTS status VARCHAR(20);

UPDATE org_department_users
SET status = CASE
    WHEN left_date IS NOT NULL AND left_date <= CURRENT_TIMESTAMP THEN 'INACTIVE'
    ELSE COALESCE(NULLIF(TRIM(status), ''), 'ACTIVE')
END;

WITH ranked_primary AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY created_at NULLS FIRST, id) AS rn
    FROM org_department_users
    WHERE is_primary = TRUE AND status = 'ACTIVE'
)
UPDATE org_department_users odu
SET is_primary = FALSE
WHERE odu.id IN (
    SELECT id FROM ranked_primary WHERE rn > 1
);

ALTER TABLE org_department_users
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN status SET DEFAULT 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_org_department_users_status ON org_department_users(status);
CREATE UNIQUE INDEX IF NOT EXISTS uk_org_department_users_primary_active
    ON org_department_users(user_id)
    WHERE is_primary = TRUE AND status = 'ACTIVE';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_org_department_users_status'
    ) THEN
        ALTER TABLE org_department_users
            ADD CONSTRAINT ck_org_department_users_status
            CHECK (status IN ('ACTIVE', 'INACTIVE')) NOT VALID;
    END IF;
END $$;

COMMENT ON COLUMN org_department_users.status IS 'Membership lifecycle status; left_date is retained as historical metadata.';

ALTER TABLE sys_menus
    ADD COLUMN IF NOT EXISTS name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS menu_type VARCHAR(20),
    ADD COLUMN IF NOT EXISTS component VARCHAR(200),
    ADD COLUMN IF NOT EXISTS visible BOOLEAN,
    ADD COLUMN IF NOT EXISTS status VARCHAR(20),
    ADD COLUMN IF NOT EXISTS external_url VARCHAR(500);

UPDATE sys_menus
SET name = COALESCE(NULLIF(TRIM(name), ''), INITCAP(REPLACE(COALESCE(code, id), '_', ' '))),
    menu_type = COALESCE(NULLIF(TRIM(menu_type), ''), 'MENU'),
    visible = COALESCE(visible, TRUE),
    status = COALESCE(NULLIF(TRIM(status), ''), 'ACTIVE');

ALTER TABLE sys_menus
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN menu_type SET NOT NULL,
    ALTER COLUMN visible SET NOT NULL,
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN menu_type SET DEFAULT 'MENU',
    ALTER COLUMN visible SET DEFAULT TRUE,
    ALTER COLUMN status SET DEFAULT 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_sys_menus_parent_offset ON sys_menus(parent_id, menu_offset);
CREATE INDEX IF NOT EXISTS idx_sys_menus_status_visible ON sys_menus(status, visible);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_sys_menus_menu_type'
    ) THEN
        ALTER TABLE sys_menus
            ADD CONSTRAINT ck_sys_menus_menu_type
            CHECK (menu_type IN ('MODULE', 'MENU', 'BUTTON')) NOT VALID;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_sys_menus_status'
    ) THEN
        ALTER TABLE sys_menus
            ADD CONSTRAINT ck_sys_menus_status
            CHECK (status IN ('ACTIVE', 'INACTIVE')) NOT VALID;
    END IF;
END $$;

COMMENT ON COLUMN sys_menus.actions IS 'Deprecated for authorization. Retained only for temporary UI compatibility.';
COMMENT ON COLUMN sys_menus.menu_type IS 'Frontend navigation type: MODULE, MENU or BUTTON.';

COMMENT ON TABLE idp_permissions IS 'Legacy mixed permission-assignment table. Read-only after RBAC phase-1 migration.';
COMMENT ON TABLE org_department_permissions IS 'Deprecated legacy department permission table. Do not write new records.';

CREATE TABLE IF NOT EXISTS idp_permission_definitions (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    module_code VARCHAR(50) NOT NULL,
    action_code VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT uk_idp_permission_definitions_module_action UNIQUE (module_code, action_code),
    CONSTRAINT ck_idp_permission_definitions_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX IF NOT EXISTS idx_idp_permission_definitions_module_code ON idp_permission_definitions(module_code);
CREATE INDEX IF NOT EXISTS idx_idp_permission_definitions_status ON idp_permission_definitions(status);

COMMENT ON TABLE idp_permission_definitions IS 'Atomic RBAC permission definitions used by roles, APIs and menus.';
COMMENT ON COLUMN idp_permission_definitions.code IS 'Stable business permission code, for example USER_CREATE.';

CREATE TABLE IF NOT EXISTS idp_role_permissions (
    id VARCHAR(36) PRIMARY KEY,
    group_id VARCHAR(36) NOT NULL REFERENCES idp_groups(id),
    permission_id VARCHAR(36) NOT NULL REFERENCES idp_permission_definitions(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    effective_from TIMESTAMP,
    effective_to TIMESTAMP,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT uk_idp_role_permissions_group_permission UNIQUE (group_id, permission_id),
    CONSTRAINT ck_idp_role_permissions_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_idp_role_permissions_effective_range CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
);

CREATE INDEX IF NOT EXISTS idx_idp_role_permissions_group_status ON idp_role_permissions(group_id, status);
CREATE INDEX IF NOT EXISTS idx_idp_role_permissions_permission_status ON idp_role_permissions(permission_id, status);

CREATE TABLE IF NOT EXISTS idp_permission_apis (
    id VARCHAR(36) PRIMARY KEY,
    permission_id VARCHAR(36) NOT NULL REFERENCES idp_permission_definitions(id),
    http_method VARCHAR(10) NOT NULL,
    uri_pattern VARCHAR(300) NOT NULL,
    match_type VARCHAR(20) NOT NULL DEFAULT 'EXACT',
    service_code VARCHAR(50) NOT NULL DEFAULT 'LEDGER_SERVICE',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    priority INTEGER NOT NULL DEFAULT 0,
    is_allow BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT uk_idp_permission_apis_permission_http_uri_service UNIQUE (permission_id, http_method, uri_pattern, service_code),
    CONSTRAINT ck_idp_permission_apis_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_idp_permission_apis_match_type CHECK (match_type IN ('EXACT', 'ANT_PATH'))
);

CREATE INDEX IF NOT EXISTS idx_idp_permission_apis_lookup ON idp_permission_apis(service_code, http_method, status);
CREATE INDEX IF NOT EXISTS idx_idp_permission_apis_uri_pattern ON idp_permission_apis(uri_pattern);

COMMENT ON TABLE idp_permission_apis IS 'Maps business permissions to protected APIs. Public APIs use is_allow = true.';
COMMENT ON COLUMN idp_permission_apis.uri_pattern IS 'Normalized Spring-style route without query string or context path.';

CREATE TABLE IF NOT EXISTS org_department_user_roles (
    id VARCHAR(36) PRIMARY KEY,
    department_user_id VARCHAR(36) NOT NULL REFERENCES org_department_users(id),
    group_id VARCHAR(36) NOT NULL REFERENCES idp_groups(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    effective_from TIMESTAMP,
    effective_to TIMESTAMP,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT uk_org_department_user_roles_membership_role UNIQUE (department_user_id, group_id),
    CONSTRAINT ck_org_department_user_roles_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_org_department_user_roles_effective_range CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
);

CREATE INDEX IF NOT EXISTS idx_org_department_user_roles_membership_status ON org_department_user_roles(department_user_id, status);
CREATE INDEX IF NOT EXISTS idx_org_department_user_roles_group_status ON org_department_user_roles(group_id, status);



CREATE TABLE IF NOT EXISTS sys_menu_permissions (
    id VARCHAR(36) PRIMARY KEY,
    menu_id VARCHAR(36) NOT NULL REFERENCES sys_menus(id),
    permission_id VARCHAR(36) NOT NULL REFERENCES idp_permission_definitions(id),
    display_action VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT uk_sys_menu_permissions_menu_permission UNIQUE (menu_id, permission_id),
    CONSTRAINT ck_sys_menu_permissions_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX IF NOT EXISTS idx_sys_menu_permissions_menu_status ON sys_menu_permissions(menu_id, status);
CREATE INDEX IF NOT EXISTS idx_sys_menu_permissions_permission_status ON sys_menu_permissions(permission_id, status);

COMMENT ON TABLE sys_menu_permissions IS 'Maps UI menus and buttons to RBAC permissions. Not used for API authorization.';

WITH legacy_menu_actions AS (
    SELECT DISTINCT
           m.id AS menu_id,
           UPPER(m.code) AS module_code,
           UPPER(TRIM(action_item)) AS action_code,
           COALESCE(NULLIF(TRIM(m.name), ''), INITCAP(REPLACE(m.code, '_', ' '))) AS menu_name
    FROM idp_permissions p
    JOIN sys_menus m ON m.id = p.menu_id
    CROSS JOIN LATERAL unnest(string_to_array(COALESCE(p.actions, ''), ';')) AS action_item
    WHERE p.menu_id IS NOT NULL
      AND TRIM(COALESCE(action_item, '')) <> ''
    UNION
    SELECT DISTINCT
           m.id AS menu_id,
           UPPER(m.code) AS module_code,
           UPPER(TRIM(action_item)) AS action_code,
           COALESCE(NULLIF(TRIM(m.name), ''), INITCAP(REPLACE(m.code, '_', ' '))) AS menu_name
    FROM org_department_permissions p
    JOIN sys_menus m ON m.id = p.menu_id
    CROSS JOIN LATERAL unnest(string_to_array(COALESCE(p.actions, ''), ';')) AS action_item
    WHERE p.menu_id IS NOT NULL
      AND TRIM(COALESCE(action_item, '')) <> ''
), normalized_permissions AS (
    SELECT DISTINCT
           menu_id,
           module_code,
           action_code,
           REGEXP_REPLACE(module_code, '(_MANAGEMENT|_MODULE|_MENU)$', '') || '_' || action_code AS permission_code,
           action_code || ' ' || menu_name AS permission_name,
           REGEXP_REPLACE(module_code, '(_MANAGEMENT|_MODULE|_MENU)$', '') AS resource_type
    FROM legacy_menu_actions
)
INSERT INTO idp_permission_definitions (
    id,
    code,
    name,
    module_code,
    action_code,
    resource_type,
    status,
    description,
    created_at,
    created_by,
    updated_at,
    updated_by
)
SELECT
    LOWER(SUBSTRING(md5(permission_code) FROM 1 FOR 8) || '-' ||
          SUBSTRING(md5(permission_code) FROM 9 FOR 4) || '-' ||
          SUBSTRING(md5(permission_code) FROM 13 FOR 4) || '-' ||
          SUBSTRING(md5(permission_code) FROM 17 FOR 4) || '-' ||
          SUBSTRING(md5(permission_code) FROM 21 FOR 12)),
    permission_code,
    permission_name,
    module_code,
    action_code,
    resource_type,
    'ACTIVE',
    'Migrated from legacy menu-action permissions',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system'
FROM normalized_permissions np
WHERE NOT EXISTS (
    SELECT 1 FROM idp_permission_definitions ipd WHERE ipd.code = np.permission_code
);

WITH normalized_permissions AS (
    SELECT DISTINCT
           m.id AS menu_id,
           REGEXP_REPLACE(UPPER(m.code), '(_MANAGEMENT|_MODULE|_MENU)$', '') || '_' || UPPER(TRIM(action_item)) AS permission_code,
           UPPER(TRIM(action_item)) AS action_code
    FROM idp_permissions p
    JOIN sys_menus m ON m.id = p.menu_id
    CROSS JOIN LATERAL unnest(string_to_array(COALESCE(p.actions, ''), ';')) AS action_item
    WHERE p.menu_id IS NOT NULL
      AND TRIM(COALESCE(action_item, '')) <> ''
    UNION
    SELECT DISTINCT
           m.id AS menu_id,
           REGEXP_REPLACE(UPPER(m.code), '(_MANAGEMENT|_MODULE|_MENU)$', '') || '_' || UPPER(TRIM(action_item)) AS permission_code,
           UPPER(TRIM(action_item)) AS action_code
    FROM org_department_permissions p
    JOIN sys_menus m ON m.id = p.menu_id
    CROSS JOIN LATERAL unnest(string_to_array(COALESCE(p.actions, ''), ';')) AS action_item
    WHERE p.menu_id IS NOT NULL
      AND TRIM(COALESCE(action_item, '')) <> ''
)
INSERT INTO sys_menu_permissions (
    id,
    menu_id,
    permission_id,
    display_action,
    status,
    description,
    created_at,
    created_by,
    updated_at,
    updated_by
)
SELECT
    LOWER(SUBSTRING(md5(np.menu_id || ':' || ipd.id) FROM 1 FOR 8) || '-' ||
          SUBSTRING(md5(np.menu_id || ':' || ipd.id) FROM 9 FOR 4) || '-' ||
          SUBSTRING(md5(np.menu_id || ':' || ipd.id) FROM 13 FOR 4) || '-' ||
          SUBSTRING(md5(np.menu_id || ':' || ipd.id) FROM 17 FOR 4) || '-' ||
          SUBSTRING(md5(np.menu_id || ':' || ipd.id) FROM 21 FOR 12)),
    np.menu_id,
    ipd.id,
    np.action_code,
    'ACTIVE',
    'Migrated from legacy menu-action permissions',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system'
FROM normalized_permissions np
JOIN idp_permission_definitions ipd ON ipd.code = np.permission_code
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu_permissions smp
    WHERE smp.menu_id = np.menu_id AND smp.permission_id = ipd.id
);

WITH group_permission_actions AS (
    SELECT DISTINCT
           p.group_id,
           REGEXP_REPLACE(UPPER(m.code), '(_MANAGEMENT|_MODULE|_MENU)$', '') || '_' || UPPER(TRIM(action_item)) AS permission_code
    FROM idp_permissions p
    JOIN sys_menus m ON m.id = p.menu_id
    CROSS JOIN LATERAL unnest(string_to_array(COALESCE(p.actions, ''), ';')) AS action_item
    WHERE p.group_id IS NOT NULL
      AND p.menu_id IS NOT NULL
      AND TRIM(COALESCE(action_item, '')) <> ''
)
INSERT INTO idp_role_permissions (
    id,
    group_id,
    permission_id,
    status,
    effective_from,
    description,
    created_at,
    created_by,
    updated_at,
    updated_by
)
SELECT
    LOWER(SUBSTRING(md5(gpa.group_id || ':' || ipd.id) FROM 1 FOR 8) || '-' ||
          SUBSTRING(md5(gpa.group_id || ':' || ipd.id) FROM 9 FOR 4) || '-' ||
          SUBSTRING(md5(gpa.group_id || ':' || ipd.id) FROM 13 FOR 4) || '-' ||
          SUBSTRING(md5(gpa.group_id || ':' || ipd.id) FROM 17 FOR 4) || '-' ||
          SUBSTRING(md5(gpa.group_id || ':' || ipd.id) FROM 21 FOR 12)),
    gpa.group_id,
    ipd.id,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    'Migrated from legacy group permissions',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system'
FROM group_permission_actions gpa
JOIN idp_permission_definitions ipd ON ipd.code = gpa.permission_code
WHERE NOT EXISTS (
    SELECT 1 FROM idp_role_permissions irp
    WHERE irp.group_id = gpa.group_id AND irp.permission_id = ipd.id
);

WITH legacy_user_roles AS (
    SELECT DISTINCT
           p.user_id,
           LOWER(SUBSTRING(md5('legacy-role:' || p.user_id) FROM 1 FOR 8) || '-' ||
                 SUBSTRING(md5('legacy-role:' || p.user_id) FROM 9 FOR 4) || '-' ||
                 SUBSTRING(md5('legacy-role:' || p.user_id) FROM 13 FOR 4) || '-' ||
                 SUBSTRING(md5('legacy-role:' || p.user_id) FROM 17 FOR 4) || '-' ||
                 SUBSTRING(md5('legacy-role:' || p.user_id) FROM 21 FOR 12)) AS role_id,
           'LEGACY_USER_' || UPPER(SUBSTRING(md5(p.user_id) FROM 1 FOR 20)) AS role_code,
           'Legacy migrated user permissions ' || p.user_id AS role_name
    FROM idp_permissions p
    WHERE p.user_id IS NOT NULL
)
INSERT INTO idp_groups (
    id,
    created_at,
    created_by,
    updated_at,
    updated_by,
    description,
    code,
    name,
    is_default,
    is_super_admin,
    status,
    sort_order
)
SELECT
    lur.role_id,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    'Synthetic role generated from legacy direct user permissions',
    lur.role_code,
    lur.role_name,
    FALSE,
    FALSE,
    'ACTIVE',
    0
FROM legacy_user_roles lur
WHERE NOT EXISTS (
    SELECT 1 FROM idp_groups ig WHERE ig.id = lur.role_id
);

WITH legacy_user_roles AS (
    SELECT DISTINCT
           p.user_id,
           LOWER(SUBSTRING(md5('legacy-role:' || p.user_id) FROM 1 FOR 8) || '-' ||
                 SUBSTRING(md5('legacy-role:' || p.user_id) FROM 9 FOR 4) || '-' ||
                 SUBSTRING(md5('legacy-role:' || p.user_id) FROM 13 FOR 4) || '-' ||
                 SUBSTRING(md5('legacy-role:' || p.user_id) FROM 17 FOR 4) || '-' ||
                 SUBSTRING(md5('legacy-role:' || p.user_id) FROM 21 FOR 12)) AS role_id
    FROM idp_permissions p
    WHERE p.user_id IS NOT NULL
)
INSERT INTO idp_user_groups (
    id,
    group_id,
    user_id,
    effective_from,
    status
)
SELECT
    LOWER(SUBSTRING(md5(lur.user_id || ':' || lur.role_id) FROM 1 FOR 8) || '-' ||
          SUBSTRING(md5(lur.user_id || ':' || lur.role_id) FROM 9 FOR 4) || '-' ||
          SUBSTRING(md5(lur.user_id || ':' || lur.role_id) FROM 13 FOR 4) || '-' ||
          SUBSTRING(md5(lur.user_id || ':' || lur.role_id) FROM 17 FOR 4) || '-' ||
          SUBSTRING(md5(lur.user_id || ':' || lur.role_id) FROM 21 FOR 12)),
    lur.role_id,
    lur.user_id,
    CURRENT_TIMESTAMP,
    'ACTIVE'
FROM legacy_user_roles lur
WHERE NOT EXISTS (
    SELECT 1 FROM idp_user_groups iug WHERE iug.user_id = lur.user_id AND iug.group_id = lur.role_id
);

WITH direct_user_permission_actions AS (
    SELECT DISTINCT
           p.user_id,
           LOWER(SUBSTRING(md5('legacy-role:' || p.user_id) FROM 1 FOR 8) || '-' ||
                 SUBSTRING(md5('legacy-role:' || p.user_id) FROM 9 FOR 4) || '-' ||
                 SUBSTRING(md5('legacy-role:' || p.user_id) FROM 13 FOR 4) || '-' ||
                 SUBSTRING(md5('legacy-role:' || p.user_id) FROM 17 FOR 4) || '-' ||
                 SUBSTRING(md5('legacy-role:' || p.user_id) FROM 21 FOR 12)) AS role_id,
           REGEXP_REPLACE(UPPER(m.code), '(_MANAGEMENT|_MODULE|_MENU)$', '') || '_' || UPPER(TRIM(action_item)) AS permission_code
    FROM idp_permissions p
    JOIN sys_menus m ON m.id = p.menu_id
    CROSS JOIN LATERAL unnest(string_to_array(COALESCE(p.actions, ''), ';')) AS action_item
    WHERE p.user_id IS NOT NULL
      AND p.menu_id IS NOT NULL
      AND TRIM(COALESCE(action_item, '')) <> ''
)
INSERT INTO idp_role_permissions (
    id,
    group_id,
    permission_id,
    status,
    effective_from,
    description,
    created_at,
    created_by,
    updated_at,
    updated_by
)
SELECT
    LOWER(SUBSTRING(md5(dupa.role_id || ':' || ipd.id) FROM 1 FOR 8) || '-' ||
          SUBSTRING(md5(dupa.role_id || ':' || ipd.id) FROM 9 FOR 4) || '-' ||
          SUBSTRING(md5(dupa.role_id || ':' || ipd.id) FROM 13 FOR 4) || '-' ||
          SUBSTRING(md5(dupa.role_id || ':' || ipd.id) FROM 17 FOR 4) || '-' ||
          SUBSTRING(md5(dupa.role_id || ':' || ipd.id) FROM 21 FOR 12)),
    dupa.role_id,
    ipd.id,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    'Migrated from legacy direct user permissions',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system'
FROM direct_user_permission_actions dupa
JOIN idp_permission_definitions ipd ON ipd.code = dupa.permission_code
WHERE NOT EXISTS (
    SELECT 1 FROM idp_role_permissions irp
    WHERE irp.group_id = dupa.role_id AND irp.permission_id = ipd.id
);
