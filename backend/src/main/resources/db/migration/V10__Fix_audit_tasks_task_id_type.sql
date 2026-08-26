-- audit_tasks.task_id was created as BIGINT (V4), but task.id is a VARCHAR
-- NanoId (same shape as audit_users.users_id for the users table). Every
-- write to task fires tasks_audit_trigger -> log_tasks_audit(), which fails
-- trying to insert task.id into a BIGINT column. The table has always been
-- empty, since nothing successfully wrote to task before this fix.
ALTER TABLE audit_tasks
    ALTER COLUMN task_id TYPE VARCHAR(21) USING task_id::text;
