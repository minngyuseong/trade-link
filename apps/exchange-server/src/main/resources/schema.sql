CREATE TABLE IF NOT EXISTS exchange_order (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    broker_code VARCHAR(10) NOT NULL,
    broker_order_id BIGINT NOT NULL,
    stock_code VARCHAR(10) NOT NULL,
    side VARCHAR(4) NOT NULL,
    price BIGINT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(10) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_exchange_order_broker_order UNIQUE (broker_code, broker_order_id),
    INDEX idx_exchange_order_book (stock_code, status, side, price, created_at)
);
