// JDBC SQL로 저장
package com.tradelink.krx.repository;

import com.tradelink.krx.dto.OrderRequest;
import com.tradelink.krx.dto.OrderDetailsResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
public class OrderRepository {
    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long insertWaiting(OrderRequest order) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO exchange_order
                        (user_id, broker_code, broker_order_id, stock_code, side, price, quantity, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, 'WAITING')
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, order.userId());
            statement.setString(2, order.brokerCode());
            statement.setLong(3, order.brokerOrderId());
            statement.setString(4, order.stockCode());
            statement.setString(5, order.side());
            statement.setLong(6, order.price());
            statement.setInt(7, order.quantity());
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Database did not return an exchange order ID");
        }
        return key.longValue();
    }

    public OrderDetailsResponse findByExchangeOrderId(long exchangeOrderId) {
        List<OrderDetailsResponse> orders = jdbcTemplate.query("""
                SELECT id, user_id, broker_code, broker_order_id, stock_code,
                       side, price, quantity, status, created_at
                FROM exchange_order
                WHERE id = ?
                """, (resultSet, rowNumber) -> mapOrder(resultSet), exchangeOrderId);

        return orders.stream().findFirst().orElse(null);
    }

    public OrderDetailsResponse findByBrokerOrderId(String brokerCode, long brokerOrderId) {
        List<OrderDetailsResponse> orders = jdbcTemplate.query("""
                SELECT id, user_id, broker_code, broker_order_id, stock_code,
                       side, price, quantity, status, created_at
                FROM exchange_order
                WHERE broker_code = ? AND broker_order_id = ?
                """, (resultSet, rowNumber) -> mapOrder(resultSet), brokerCode, brokerOrderId);

        return orders.stream().findFirst().orElse(null);
    }

    public List<OrderDetailsResponse> findWaitingOrders() {
        return jdbcTemplate.query("""
                SELECT id, user_id, broker_code, broker_order_id, stock_code,
                       side, price, quantity, status, created_at
                FROM exchange_order
                WHERE status = 'WAITING'
                ORDER BY created_at, id
                """, (resultSet, rowNumber) -> mapOrder(resultSet));
    }

    public List<OrderDetailsResponse> lockWaitingOrders(long firstOrderId, long secondOrderId) {
        long lowerOrderId = Math.min(firstOrderId, secondOrderId);
        long higherOrderId = Math.max(firstOrderId, secondOrderId);
        return jdbcTemplate.query("""
                SELECT id, user_id, broker_code, broker_order_id, stock_code,
                       side, price, quantity, status, created_at
                FROM exchange_order
                WHERE id IN (?, ?) AND status = 'WAITING'
                ORDER BY id
                FOR UPDATE
                """, (resultSet, rowNumber) -> mapOrder(resultSet), lowerOrderId, higherOrderId);
    }

    public int markMatchedIfWaiting(long firstOrderId, long secondOrderId) {
        return jdbcTemplate.update("""
                UPDATE exchange_order
                SET status = 'MATCHED'
                WHERE id IN (?, ?) AND status = 'WAITING'
                """, firstOrderId, secondOrderId);
    }

    private OrderDetailsResponse mapOrder(ResultSet resultSet) throws SQLException {
        String status = resultSet.getString("status");
        return new OrderDetailsResponse(
                resultSet.getLong("id"),
                resultSet.getLong("user_id"),
                resultSet.getString("broker_code"),
                resultSet.getLong("broker_order_id"),
                resultSet.getString("stock_code"),
                resultSet.getString("side"),
                resultSet.getLong("price"),
                resultSet.getInt("quantity"),
                status,
                "MATCHED".equals(status),
                resultSet.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
