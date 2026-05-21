CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    yookassa_payment_id VARCHAR(64),
    buyer_user_id BIGINT NOT NULL REFERENCES users(id),
    amount_rub NUMERIC(12, 2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE payment_orders (
    payment_id BIGINT NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    shop_order_id BIGINT NOT NULL REFERENCES shop_orders(id) ON DELETE CASCADE,
    PRIMARY KEY (payment_id, shop_order_id)
);

CREATE INDEX idx_payments_buyer ON payments(buyer_user_id);
CREATE INDEX idx_payments_yookassa ON payments(yookassa_payment_id);
