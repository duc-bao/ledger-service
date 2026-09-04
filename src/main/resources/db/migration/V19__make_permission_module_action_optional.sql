-- Loai bo rang buoc bat buoc va unique constraint tren module_code va action_code
ALTER TABLE idp_permission_definitions DROP CONSTRAINT IF EXISTS uk_idp_permission_definitions_module_action;

ALTER TABLE idp_permission_definitions ALTER COLUMN module_code DROP NOT NULL;
ALTER TABLE idp_permission_definitions ALTER COLUMN action_code DROP NOT NULL;
