package com.tradelink.exchange.matching;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class TradeRepository {

	public record TradeView(
			long tradeId,
			String stockCode,
			long price,
			long quantity,
			String buyBrokerCode,
			long buyBrokerOrderId,
			String sellBrokerCode,
			long sellBrokerOrderId,
			LocalDateTime createdAt
	) {}

	private final JdbcTemplate jdbcTemplate;

	public TradeRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Persists against the proposed trade schema. The SQL must be validated with the
	 * target MySQL schema before this method is used by an enabled worker.
	 */
	public long insert(MatchResult match, String stockCode) {
		long price = match.price().longValueExact();
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection.prepareStatement("""
					INSERT INTO trade
					    (buy_order_id, sell_order_id, stock_code, price, quantity)
					VALUES (?, ?, ?, ?, ?)
					""", Statement.RETURN_GENERATED_KEYS);
			statement.setLong(1, match.buyOrderId());
			statement.setLong(2, match.sellOrderId());
			statement.setString(3, stockCode);
			statement.setLong(4, price);
			statement.setLong(5, match.quantity());
			return statement;
		}, keyHolder);

		Number key = keyHolder.getKey();
		if (key == null) {
			throw new IllegalStateException("Database did not return a trade ID");
		}
		return key.longValue();
	}

	public List<TradeView> findRecentTrades() {
		return jdbcTemplate.query("""
				SELECT t.id, t.stock_code, t.price, t.quantity, t.created_at,
				       buy_order.broker_code AS buy_broker_code,
				       buy_order.broker_order_id AS buy_broker_order_id,
				       sell_order.broker_code AS sell_broker_code,
				       sell_order.broker_order_id AS sell_broker_order_id
				FROM trade t
				JOIN exchange_order buy_order ON buy_order.id = t.buy_order_id
				JOIN exchange_order sell_order ON sell_order.id = t.sell_order_id
				ORDER BY t.created_at DESC, t.id DESC
				LIMIT 100
				""", (resultSet, rowNumber) -> mapTrade(resultSet));
	}

	private TradeView mapTrade(ResultSet resultSet) throws SQLException {
		return new TradeView(
				resultSet.getLong("id"),
				resultSet.getString("stock_code"),
				resultSet.getLong("price"),
				resultSet.getLong("quantity"),
				resultSet.getString("buy_broker_code"),
				resultSet.getLong("buy_broker_order_id"),
				resultSet.getString("sell_broker_code"),
				resultSet.getLong("sell_broker_order_id"),
				resultSet.getTimestamp("created_at").toLocalDateTime()
		);
	}
}
