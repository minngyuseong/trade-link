CREATE TABLE IF NOT EXISTS broker_cash (
    broker_code VARCHAR(10) NOT NULL PRIMARY KEY,
    amount BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS broker_stock (
    broker_code VARCHAR(10) NOT NULL,
    stock_code VARCHAR(10) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    PRIMARY KEY (broker_code, stock_code)
);

CREATE TABLE IF NOT EXISTS settlement (
    trade_id BIGINT NOT NULL PRIMARY KEY,
    status VARCHAR(16) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE TABLE IF NOT EXISTS settlement_audit (
    trade_id BIGINT NOT NULL PRIMARY KEY,
    stock_code VARCHAR(10) NOT NULL,
    price BIGINT NOT NULL,
    quantity INT NOT NULL,
    buy_broker_code VARCHAR(10) NOT NULL,
    buy_broker_order_id BIGINT NOT NULL,
    sell_broker_code VARCHAR(10) NOT NULL,
    sell_broker_order_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    settled_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_settlement_audit_time (settled_at)
);
