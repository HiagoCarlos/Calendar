-- V1 - create tables
CREATE TYPE notification_type AS ENUM ('email', 'notificacao');


CREATE TABLE users
(
    id              char(21) PRIMARY KEY,
    avatar          text,
    name            varchar(200),
    email           varchar(200),
    email_confirmed boolean,
    otp             varchar(8),
    otp_expiration  timestamp,
    password        varchar(100),
    created_at      timestamptz,
    updated_at      timestamptz
);


CREATE TABLE category
(
    id         integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    char(21) REFERENCES users (id),
    color      varchar(6),
    created_at timestamptz,
    title      varchar
);


CREATE TABLE task
(
    all_day         boolean,
    id              integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         char(21) REFERENCES users (id),
    category_id     integer REFERENCES category (id),
    timezone        text,
    location        text,
    title           varchar(255),
    description     text,
    status          varchar,
    starts_at       timestamptz,
    ends_at         timestamptz,
    repeat          integer,
    repeat_interval varchar(15)
);


CREATE TABLE configuration
(
    id             integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id        char(21) REFERENCES users (id),
    theme          varchar,
    time_format    varchar,
    update_at      timestamptz,
    week_start_day varchar,
    created_at     timestamptz,
    default_view   varchar
);


CREATE TABLE notification
(
    id          integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     char(21) REFERENCES users (id),
    time_before timestamptz,
    type        notification_type,
    task_id     integer REFERENCES task (id),
    read        boolean
);

CREATE TABLE log
(
    id         integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    char(21) REFERENCES users (id),
    table_name text,
    table_id   text,
    type       varchar(32),
    log        text,
    created_at timestamptz
);
