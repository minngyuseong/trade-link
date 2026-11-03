INSERT INTO accounts(account_no, user_id, cash)
VALUES ('2001', 1, 1000000000)
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

INSERT INTO holdings(account_no, stock_code, quantity)
VALUES ('2001', '005930', 100), ('2001', '000660', 100)
ON DUPLICATE KEY UPDATE quantity = quantity;
