-- Отображаемое имя при регистрации (синхронизация с фронтом)

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS full_name VARCHAR(255);
