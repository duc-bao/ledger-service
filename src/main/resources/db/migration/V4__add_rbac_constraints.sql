-- Ensure one user only maps to one role(group)
DELETE FROM idp_user_groups ug
WHERE ug.id NOT IN (
    SELECT MIN(id)
    FROM idp_user_groups
    GROUP BY user_id
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_idp_user_groups_user_id ON idp_user_groups(user_id);

DO $$
BEGIN
    ALTER TABLE idp_user_groups
        ADD CONSTRAINT fk_user_groups_user
        FOREIGN KEY (user_id) REFERENCES idp_users(id)
        ON DELETE CASCADE;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE idp_user_groups
        ADD CONSTRAINT fk_user_groups_group
        FOREIGN KEY (group_id) REFERENCES idp_groups(id)
        ON DELETE CASCADE;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE idp_permissions
        ADD CONSTRAINT fk_permissions_group
        FOREIGN KEY (group_id) REFERENCES idp_groups(id)
        ON DELETE CASCADE;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE idp_permissions
        ADD CONSTRAINT fk_permissions_menu
        FOREIGN KEY (menu_id) REFERENCES sys_menus(id)
        ON DELETE CASCADE;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;
