-- Plain varchar, not a native Postgres enum: validation happens at the
-- application boundary (Jackson rejects an unrecognized value with a 400
-- before it reaches the service), same tradeoff as repeat_interval/status
-- but with the enum giving real type safety in Java.
ALTER TABLE task
    ADD COLUMN priority VARCHAR(10);
