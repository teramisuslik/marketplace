-- Товары без продавца не проходят checkout; привязываем к первому известному seller_id.
UPDATE products
SET seller_id = (SELECT MIN(p2.seller_id) FROM products p2 WHERE p2.seller_id IS NOT NULL)
WHERE seller_id IS NULL
  AND EXISTS (SELECT 1 FROM products p2 WHERE p2.seller_id IS NOT NULL);
