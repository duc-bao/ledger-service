ALTER TABLE idp_users
    ADD COLUMN IF NOT EXISTS two_factor_enabled BOOLEAN NOT NULL DEFAULT TRUE;

COMMENT ON COLUMN idp_users.two_factor_enabled IS 'Controls whether login requires email OTP after password verification.';

CREATE TABLE IF NOT EXISTS org_company_user_roles (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    description VARCHAR(500),
    company_id VARCHAR(36) NOT NULL REFERENCES companies(id),
    user_id VARCHAR(36) NOT NULL REFERENCES idp_users(id),
    group_id VARCHAR(36) NOT NULL REFERENCES idp_groups(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    effective_from TIMESTAMP,
    effective_to TIMESTAMP,
    CONSTRAINT uk_org_company_user_roles_company_user_group UNIQUE (company_id, user_id, group_id),
    CONSTRAINT ck_org_company_user_roles_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_org_company_user_roles_effective_range CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
);

CREATE INDEX IF NOT EXISTS idx_org_company_user_roles_company_status ON org_company_user_roles(company_id, status);
CREATE INDEX IF NOT EXISTS idx_org_company_user_roles_user_status ON org_company_user_roles(user_id, status);
CREATE INDEX IF NOT EXISTS idx_org_company_user_roles_group_status ON org_company_user_roles(group_id, status);
