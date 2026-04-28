ALTER TABLE sys_menus
    DROP COLUMN IF EXISTS name;

DROP INDEX IF EXISTS idx_sys_menus_name;
