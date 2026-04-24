-- Схема БД Products: таблица products + последовательность под GenerationType.AUTO (allocation 50).

CREATE SEQUENCE IF NOT EXISTS products_seq
    START WITH 1
    INCREMENT BY 50
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE TABLE IF NOT EXISTS products (
    id                 BIGINT       NOT NULL,
    name               VARCHAR(255) NOT NULL,
    description        VARCHAR(255),
    count_of_product   INTEGER,
    rating             REAL,
    seller_id          BIGINT,
    CONSTRAINT products_pkey PRIMARY KEY (id),
    CONSTRAINT uk_products_name UNIQUE (name)
);
