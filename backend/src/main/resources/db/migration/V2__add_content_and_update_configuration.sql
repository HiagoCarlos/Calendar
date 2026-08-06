-- V2 - add content column to notification and align configuration table with updated ER diagram

ALTER TABLE notification
    ADD COLUMN content text;

CREATE TYPE theme_type AS ENUM ('light', 'dark', 'system');
CREATE TYPE time_format_type AS ENUM ('h24', 'h12');
CREATE TYPE view_type AS ENUM ('day', 'week', 'month');

ALTER TABLE configuration
    ADD COLUMN language varchar(10);

ALTER TABLE configuration
ALTER COLUMN theme TYPE theme_type USING theme::theme_type;

ALTER TABLE configuration
ALTER COLUMN time_format TYPE time_format_type USING time_format::time_format_type;

ALTER TABLE configuration
ALTER COLUMN default_view TYPE view_type USING default_view::view_type;

ALTER TABLE configuration
ALTER COLUMN week_start_day TYPE smallint USING week_start_day::smallint;