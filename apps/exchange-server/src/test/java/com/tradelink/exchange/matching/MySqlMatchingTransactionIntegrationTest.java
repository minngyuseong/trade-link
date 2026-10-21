package com.tradelink.exchange.matching;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.tradelink.exchange.config.CalculationConfig;
import com.tradelink.krx.dto.OrderRequest;
import com.tradelink.krx.repository.OrderRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = MySqlMatchingTransactionIntegrationTest.TestConfiguration.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MySqlMatchingTransactionIntegrationTest {

	private static final AtomicLong ORDER_SEQUENCE = new AtomicLong(System.currentTimeMillis());

	private final JdbcTemplate jdbcTemplate;
	private final OrderRepository orderRepository;
	private final TradeRepository tradeRepository;
	private final MatchingTransactionService matchingTransactionService;
	private final TransactionTemplate requiresNew;
	private long initialOrderCount;
	private long initialTradeCount;

	@Autowired
	MySqlMatchingTransactionIntegrationTest(JdbcTemplate jdbcTemplate,
			OrderRepository orderRepository,
			TradeRepository tradeRepository,
			MatchingTransactionService matchingTransactionService,
			PlatformTransactionManager transactionManager) {
		this.jdbcTemplate = jdbcTemplate;
		this.orderRepository = orderRepository;
		this.tradeRepository = tradeRepository;
		this.matchingTransactionService = matchingTransactionService;
		this.requiresNew = new TransactionTemplate(transactionManager);
		this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	}

	@BeforeAll
	void captureInitialCounts() {
		initialOrderCount = count("exchange_order");
		initialTradeCount = count("trade");
		assertSchemaMatchesRepositorySql();
	}

	@AfterAll
	void verifyNoTestDataRemains() {
		assertThat(count("exchange_order")).isEqualTo(initialOrderCount);
		assertThat(count("trade")).isEqualTo(initialTradeCount);
	}

	@Test
	void schemaMatchesRepositorySql() {
		assertSchemaMatchesRepositorySql();
	}

	private void assertSchemaMatchesRepositorySql() {
		assertThat(columnsOf("exchange_order")).containsAllEntriesOf(Map.ofEntries(
				Map.entry("id", "bigint"),
				Map.entry("user_id", "bigint"),
				Map.entry("broker_code", "varchar"),
				Map.entry("broker_order_id", "bigint"),
				Map.entry("stock_code", "varchar"),
				Map.entry("side", "varchar"),
				Map.entry("price", "bigint"),
				Map.entry("quantity", "int"),
				Map.entry("status", "varchar"),
				Map.entry("created_at", "datetime")
		));
		assertThat(columnsOf("trade")).containsAllEntriesOf(Map.of(
				"id", "bigint",
				"buy_order_id", "bigint",
				"sell_order_id", "bigint",
				"stock_code", "varchar",
				"price", "bigint",
				"quantity", "int",
				"created_at", "datetime"
		));
		assertThat(tradeForeignKeyColumns()).containsExactlyInAnyOrder("buy_order_id", "sell_order_id");
	}

	@Test
	void commitsTradeAndMatchedStatusesThenCleansUpPrecisely() {
		TestOrders orders = insertWaitingPair();
		try {
			long tradeId = matchingTransactionService.execute(candidate(orders));

			assertThat(statusOf(orders.buyOrderId())).isEqualTo("MATCHED");
			assertThat(statusOf(orders.sellOrderId())).isEqualTo("MATCHED");
			assertThat(tradeCountFor(orders)).isEqualTo(1);
			assertThat(jdbcTemplate.queryForObject(
					"SELECT id FROM trade WHERE buy_order_id = ? AND sell_order_id = ?",
					Long.class, orders.buyOrderId(), orders.sellOrderId())).isEqualTo(tradeId);
		} finally {
			cleanup(orders);
		}
	}

	@Test
	void rollsBackTradeAndStatusChangesWhenTransactionFails() {
		TestOrders orders = insertWaitingPair();
		try {
			assertThatThrownBy(() -> requiresNew.executeWithoutResult(status -> {
				tradeRepository.insert(candidate(orders), "005930");
				assertThat(orderRepository.markMatchedIfWaiting(
						orders.buyOrderId(), orders.sellOrderId())).isEqualTo(2);
				throw new ForcedRollbackException();
			})).isInstanceOf(ForcedRollbackException.class);

			assertThat(statusOf(orders.buyOrderId())).isEqualTo("WAITING");
			assertThat(statusOf(orders.sellOrderId())).isEqualTo("WAITING");
			assertThat(tradeCountFor(orders)).isZero();
		} finally {
			cleanup(orders);
		}
	}

	@Test
	void preventsDuplicateMatchForTheSameOrders() {
		TestOrders orders = insertWaitingPair();
		try {
			matchingTransactionService.execute(candidate(orders));

			assertThatThrownBy(() -> matchingTransactionService.execute(candidate(orders)))
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("WAITING");
			assertThat(tradeCountFor(orders)).isEqualTo(1);
		} finally {
			cleanup(orders);
		}
	}

	private TestOrders insertWaitingPair() {
		long sequence = ORDER_SEQUENCE.incrementAndGet();
		return requiresNew.execute(status -> {
			long buyOrderId = orderRepository.insertWaiting(new OrderRequest(
					9_000_001L, "BUY", 10, 70_000L, "A", sequence, "005930"));
			long sellOrderId = orderRepository.insertWaiting(new OrderRequest(
					9_000_002L, "SELL", 10, 69_000L, "B", sequence, "005930"));
			return new TestOrders(buyOrderId, sellOrderId);
		});
	}

	private MatchResult candidate(TestOrders orders) {
		return new MatchResult(
				orders.buyOrderId(), orders.sellOrderId(), BigDecimal.valueOf(70_000), 10);
	}

	private void cleanup(TestOrders orders) {
		requiresNew.executeWithoutResult(status -> {
			jdbcTemplate.update(
					"DELETE FROM trade WHERE buy_order_id = ? AND sell_order_id = ?",
					orders.buyOrderId(), orders.sellOrderId());
			jdbcTemplate.update(
					"DELETE FROM exchange_order WHERE id IN (?, ?)",
					orders.buyOrderId(), orders.sellOrderId());
		});
	}

	private String statusOf(long orderId) {
		return jdbcTemplate.queryForObject(
				"SELECT status FROM exchange_order WHERE id = ?", String.class, orderId);
	}

	private long tradeCountFor(TestOrders orders) {
		return jdbcTemplate.queryForObject("""
				SELECT COUNT(*) FROM trade
				WHERE buy_order_id = ? AND sell_order_id = ?
				""", Long.class, orders.buyOrderId(), orders.sellOrderId());
	}

	private long count(String tableName) {
		if (!Set.of("exchange_order", "trade").contains(tableName)) {
			throw new IllegalArgumentException("Unexpected table name");
		}
		return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
	}

	private Map<String, String> columnsOf(String tableName) {
		return jdbcTemplate.query("""
				SELECT column_name, data_type
				FROM information_schema.columns
				WHERE table_schema = DATABASE() AND table_name = ?
				""", resultSet -> {
					java.util.LinkedHashMap<String, String> columns = new java.util.LinkedHashMap<>();
					while (resultSet.next()) {
						columns.put(resultSet.getString("column_name"), resultSet.getString("data_type"));
					}
					return columns;
				}, tableName);
	}

	private List<String> tradeForeignKeyColumns() {
		return jdbcTemplate.queryForList("""
				SELECT column_name
				FROM information_schema.key_column_usage
				WHERE table_schema = DATABASE()
				  AND table_name = 'trade'
				  AND referenced_table_name = 'exchange_order'
				""", String.class);
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import({
			OrderRepository.class,
			TradeRepository.class,
			MatchingTransactionService.class,
			CalculationConfig.class
	})
	static class TestConfiguration {
	}

	private record TestOrders(long buyOrderId, long sellOrderId) {
	}

	private static class ForcedRollbackException extends RuntimeException {
	}
}
