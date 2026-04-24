-- Контакты в профиле (настройки / кабинет)

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email VARCHAR(255);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS phone VARCHAR(64);
