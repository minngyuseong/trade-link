INSERT INTO broker_cash(broker_code, amount)
VALUES ('A', 1000000000), ('B', 1000000000)
ON DUPLICATE KEY UPDATE amount = amount;

INSERT INTO broker_stock(broker_code, stock_code, quantity)
VALUES
    ('A', '005930', 1000),
    ('A', '000660', 1000),
    ('B', '005930', 1000),
    ('B', '000660', 1000)
ON DUPLICATE KEY UPDATE quantity = quantity;
