-- Soft delete for category, same shape as V8 for task: existing tasks keep
-- their category_id pointing at a real (if deleted) row, instead of losing
-- the category when it's removed.
ALTER TABLE category
    ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;
