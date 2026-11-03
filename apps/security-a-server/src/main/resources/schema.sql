CREATE TABLE IF NOT EXISTS accounts (
    account_no VARCHAR(32) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    cash BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS holdings (
    account_no VARCHAR(32) NOT NULL,
    stock_code VARCHAR(10) NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (account_no, stock_code)
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_no VARCHAR(32) NOT NULL,
    stock_code VARCHAR(10) NOT NULL,
    side VARCHAR(4) NOT NULL,
    price BIGINT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(16) NOT NULL,
    exchange_order_id BIGINT NULL,
    trade_id BIGINT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_orders_user (user_id),
    INDEX idx_orders_exchange (exchange_order_id)
);

CREATE TABLE IF NOT EXISTS order_trade (
    order_id BIGINT NOT NULL,
    trade_id BIGINT NOT NULL,
    price BIGINT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (order_id, trade_id)
);

CREATE TABLE IF NOT EXISTS order_settlement (
    order_id BIGINT NOT NULL,
    trade_id BIGINT NOT NULL,
    price BIGINT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (order_id, trade_id)
);
