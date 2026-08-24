-- V1 - create tables

CREATE TYPE notification_type AS ENUM ('email', 'notificacao');


-- `users` needs to come before `configuration/task/category` because of the foreign keys pointing to it.
CREATE TABLE users
(
    id              varchar(21) PRIMARY KEY,
    name            varchar(200),
    email           varchar(200),
    email_confirmed boolean,
    avatar          text,
    otp             varchar(8),
    otp_expiration  timestamp,
    password        varchar(100),
    created_at      timestamptz,
    updated_at      timestamptz
);


CREATE TABLE configuration
(
    user_id        varchar(21) PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    theme          varchar,
    time_format    varchar,
    week_start_day varchar,
    default_view   varchar,
    created_at     timestamptz,
    updated_at     timestamptz
);


CREATE TABLE task
(
    id              varchar(21) PRIMARY KEY,
    user_id         varchar(21) REFERENCES users (id) ON DELETE CASCADE,
    category_id     varchar(21),
    title           varchar(255),
    description     text,
    timezone        text,
    location        text,
    status          varchar,
    starts_at       timestamptz,
    ends_at         timestamptz,
    repeat          integer,
    repeat_interval varchar(15),
    all_day         boolean,
    created_at      timestamptz,
    updated_at      timestamptz,
);


CREATE TABLE category
(
    id         varchar(21) PRIMARY KEY,
    user_id    varchar(21) REFERENCES users (id) ON DELETE CASCADE,
    title      varchar,
    color      varchar(6),
    created_at timestamptz,
    updated_at timestamptz
);


ALTER TABLE task
    ADD CONSTRAINT fk_task_category FOREIGN KEY (category_id) REFERENCES category (id);


CREATE TABLE notification
(
    id          varchar(21) PRIMARY KEY,
    user_id     varchar(21) REFERENCES users (id) ON DELETE CASCADE,
    time_before timestamptz,
    type        notification_type,
    task_id     varchar(21) REFERENCES task (id),
    read        boolean,
    created_at  timestamptz,
    updated_at  timestamptz
);


CREATE TABLE log
(
    id         integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    varchar(21) REFERENCES users (id),
    table_name text,
    table_id   text,
    type       varchar(32),
    log        text,
    created_at timestamptz
);