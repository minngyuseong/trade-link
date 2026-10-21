package com.tradelink.exchange.matching;

import java.sql.PreparedStatement;
import java.sql.Statement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class TradeRepository {

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
}
