CREATE TABLE IF NOT EXISTS sys_email_configs (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    description VARCHAR(500),

    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    provider_type VARCHAR(50) NOT NULL,
    host VARCHAR(255),
    port INTEGER,
    username VARCHAR(255),
    password VARCHAR(500),
    from_address VARCHAR(255),
    from_name VARCHAR(255),
    reply_to VARCHAR(255),
    protocol VARCHAR(20) DEFAULT 'smtp',
    encoding VARCHAR(50) NOT NULL DEFAULT 'UTF-8',
    auth_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    starttls_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ssl_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    debug_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    timeout_ms INTEGER,
    connection_timeout_ms INTEGER,
    write_timeout_ms INTEGER,
    max_recipients INTEGER,
    default_cc JSONB NOT NULL DEFAULT '[]'::jsonb,
    default_bcc JSONB NOT NULL DEFAULT '[]'::jsonb,
    provider_settings JSONB NOT NULL DEFAULT '{}'::jsonb,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_sys_email_configs_name ON sys_email_configs(name);
CREATE INDEX IF NOT EXISTS idx_sys_email_configs_provider_type ON sys_email_configs(provider_type);
CREATE INDEX IF NOT EXISTS idx_sys_email_configs_enabled ON sys_email_configs(enabled);
CREATE INDEX IF NOT EXISTS idx_sys_email_configs_is_default ON sys_email_configs(is_default);

CREATE TABLE IF NOT EXISTS org_departments (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    description VARCHAR(500),

    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    short_name VARCHAR(100),
    parent_id VARCHAR(36),
    ancestors VARCHAR(1000),
    sort_order INTEGER NOT NULL DEFAULT 0,
    tree_level INTEGER NOT NULL DEFAULT 0,
    manager_user_id VARCHAR(36),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_org_departments_parent_id ON org_departments(parent_id);
CREATE INDEX IF NOT EXISTS idx_org_departments_code ON org_departments(code);
CREATE INDEX IF NOT EXISTS idx_org_departments_name ON org_departments(name);
CREATE INDEX IF NOT EXISTS idx_org_departments_ancestors ON org_departments(ancestors);
CREATE INDEX IF NOT EXISTS idx_org_departments_manager_user_id ON org_departments(manager_user_id);

DO $$
BEGIN
    ALTER TABLE org_departments
        ADD CONSTRAINT fk_org_departments_parent
        FOREIGN KEY (parent_id) REFERENCES org_departments(id)
        ON DELETE SET NULL;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE org_departments
        ADD CONSTRAINT fk_org_departments_manager_user
        FOREIGN KEY (manager_user_id) REFERENCES idp_users(id)
        ON DELETE SET NULL;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE org_departments
        ADD CONSTRAINT uq_org_departments_parent_sort_order
        UNIQUE (parent_id, sort_order);
EXCEPTION
    WHEN duplicate_table THEN NULL;
    WHEN duplicate_object THEN NULL;
END $$;

CREATE TABLE IF NOT EXISTS org_department_users (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    description VARCHAR(500),

    department_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT TRUE,
    title VARCHAR(100),
    join_date TIMESTAMP,
    left_date TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_org_department_users_department_user
    ON org_department_users(department_id, user_id);
CREATE INDEX IF NOT EXISTS idx_org_department_users_department_id ON org_department_users(department_id);
CREATE INDEX IF NOT EXISTS idx_org_department_users_user_id ON org_department_users(user_id);
CREATE INDEX IF NOT EXISTS idx_org_department_users_is_primary ON org_department_users(is_primary);

DO $$
BEGIN
    ALTER TABLE org_department_users
        ADD CONSTRAINT fk_org_department_users_department
        FOREIGN KEY (department_id) REFERENCES org_departments(id)
        ON DELETE CASCADE;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE org_department_users
        ADD CONSTRAINT fk_org_department_users_user
        FOREIGN KEY (user_id) REFERENCES idp_users(id)
        ON DELETE CASCADE;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

CREATE TABLE IF NOT EXISTS org_department_permissions (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    description VARCHAR(500),

    department_id VARCHAR(36) NOT NULL,
    menu_id VARCHAR(36) NOT NULL,
    discriminator VARCHAR(100) NOT NULL DEFAULT 'DEPARTMENT',
    actions VARCHAR(700),
    inherited BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from TIMESTAMP,
    effective_to TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_org_department_permissions_department_menu_discriminator
    ON org_department_permissions(department_id, menu_id, discriminator);
CREATE INDEX IF NOT EXISTS idx_org_department_permissions_department_id ON org_department_permissions(department_id);
CREATE INDEX IF NOT EXISTS idx_org_department_permissions_menu_id ON org_department_permissions(menu_id);
CREATE INDEX IF NOT EXISTS idx_org_department_permissions_discriminator ON org_department_permissions(discriminator);

DO $$
BEGIN
    ALTER TABLE org_department_permissions
        ADD CONSTRAINT fk_org_department_permissions_department
        FOREIGN KEY (department_id) REFERENCES org_departments(id)
        ON DELETE CASCADE;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE org_department_permissions
        ADD CONSTRAINT fk_org_department_permissions_menu
        FOREIGN KEY (menu_id) REFERENCES sys_menus(id)
        ON DELETE CASCADE;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;
