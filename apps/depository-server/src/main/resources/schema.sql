CREATE TABLE IF NOT EXISTS broker_cash (
    broker_code VARCHAR(10) PRIMARY KEY,
    amount BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS broker_stock (
    broker_code VARCHAR(10) NOT NULL,
    stock_code VARCHAR(10) NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (broker_code, stock_code)
);

CREATE TABLE IF NOT EXISTS settlement (
    trade_id BIGINT PRIMARY KEY,
    status VARCHAR(16) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);
