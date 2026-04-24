-- Цена товара для каталога и корзины (рубли, дробная часть допустима)

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS price DOUBLE PRECISION NOT NULL DEFAULT 0;
