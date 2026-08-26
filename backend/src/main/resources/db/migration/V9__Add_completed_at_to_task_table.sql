-- Adiciona a coluna completed_at à tabela task, que indica quando a task foi concluída
ALTER TABLE task
    ADD COLUMN completed_at TIMESTAMP WITH TIME ZONE;
