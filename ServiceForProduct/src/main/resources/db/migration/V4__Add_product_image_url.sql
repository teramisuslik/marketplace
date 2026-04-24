-- URL или путь картинки товара (относительный к фронту или абсолютный)

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(2048);
