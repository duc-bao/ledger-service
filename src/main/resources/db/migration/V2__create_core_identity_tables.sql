CREATE TABLE IF NOT EXISTS idp_groups (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    description VARCHAR(500),
    code VARCHAR(50) UNIQUE,
    name VARCHAR(255) UNIQUE,
    is_default BOOLEAN,
    is_super_admin BOOLEAN
);

CREATE TABLE IF NOT EXISTS idp_users (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    description VARCHAR(500),
    user_name VARCHAR(50) UNIQUE,
    password VARCHAR(255),
    email VARCHAR(50) UNIQUE,
    phone VARCHAR(15) UNIQUE,
    full_name VARCHAR(100),
    user_type VARCHAR(100),
    require_change BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS idp_user_groups (
    id VARCHAR(36) PRIMARY KEY,
    group_id VARCHAR(36),
    user_id VARCHAR(36)
);

CREATE TABLE IF NOT EXISTS idp_permissions (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    description VARCHAR(500),
    menu_id VARCHAR(36),
    group_id VARCHAR(36),
    user_id VARCHAR(36),
    discriminator VARCHAR(100),
    actions VARCHAR(700)
);

CREATE TABLE IF NOT EXISTS sys_menus (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    description VARCHAR(500),
    code VARCHAR(50) UNIQUE,
    name VARCHAR(255) UNIQUE,
    path VARCHAR(255),
    ancestors VARCHAR(700),
    parent_id VARCHAR(36),
    icon VARCHAR(255),
    actions VARCHAR(700),
    menu_offset INTEGER
);

CREATE INDEX IF NOT EXISTS idx_idp_user_groups_group_id ON idp_user_groups(group_id);
CREATE INDEX IF NOT EXISTS idx_idp_user_groups_user_id ON idp_user_groups(user_id);

CREATE INDEX IF NOT EXISTS idx_idp_permissions_menu_id ON idp_permissions(menu_id);
CREATE INDEX IF NOT EXISTS idx_idp_permissions_group_id ON idp_permissions(group_id);
CREATE INDEX IF NOT EXISTS idx_idp_permissions_user_id ON idp_permissions(user_id);
CREATE INDEX IF NOT EXISTS idx_idp_permissions_discriminator ON idp_permissions(discriminator);

CREATE INDEX IF NOT EXISTS idx_idp_users_user_name ON idp_users(user_name);
CREATE INDEX IF NOT EXISTS idx_idp_users_email ON idp_users(email);
CREATE INDEX IF NOT EXISTS idx_idp_users_full_name ON idp_users(full_name);
CREATE INDEX IF NOT EXISTS idx_idp_users_phone ON idp_users(phone);

CREATE INDEX IF NOT EXISTS idx_sys_menus_name ON sys_menus(name);
CREATE INDEX IF NOT EXISTS idx_sys_menus_code ON sys_menus(code);
CREATE INDEX IF NOT EXISTS idx_sys_menus_parent_id ON sys_menus(parent_id);
CREATE INDEX IF NOT EXISTS idx_sys_menus_ancestors ON sys_menus(ancestors);
