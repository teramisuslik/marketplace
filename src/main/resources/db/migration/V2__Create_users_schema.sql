-- Схема БД Users (сервис marketplace): таблица users + последовательность под GenerationType.AUTO (allocation 50).

CREATE SEQUENCE IF NOT EXISTS users_seq
    START WITH 1
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE TABLE IF NOT EXISTS users (
    id           BIGINT       NOT NULL,
    username     VARCHAR(255) NOT NULL,
    password     VARCHAR(255) NOT NULL,
    role         VARCHAR(255),
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username)
);
