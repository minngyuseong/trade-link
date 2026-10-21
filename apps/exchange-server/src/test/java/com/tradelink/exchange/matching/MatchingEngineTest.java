package com.tradelink.exchange.matching;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingEngineTest {

	private final MatchingEngine matchingEngine = new MatchingEngine();

	@Test
	void buyChoosesLowestSellPriceBeforeTime() {
		MatchingOrder incomingBuy = order(100, OrderSide.BUY, "72000", 10, "10:00:03Z");
		MatchingOrder olderButExpensiveSell = order(1, OrderSide.SELL, "71000", 10, "10:00:01Z");
		MatchingOrder newerButCheaperSell = order(2, OrderSide.SELL, "70000", 10, "10:00:02Z");

		assertThat(matchingEngine.match(incomingBuy, List.of(olderButExpensiveSell, newerButCheaperSell)))
				.contains(new MatchResult(100, 2, new BigDecimal("70000"), 10));
	}

	@Test
	void sellChoosesHighestBuyPriceBeforeTime() {
		MatchingOrder incomingSell = order(100, OrderSide.SELL, "69000", 10, "10:00:03Z");
		MatchingOrder olderButLowerBuy = order(1, OrderSide.BUY, "70000", 10, "10:00:01Z");
		MatchingOrder newerButHigherBuy = order(2, OrderSide.BUY, "71000", 10, "10:00:02Z");

		assertThat(matchingEngine.match(incomingSell, List.of(olderButLowerBuy, newerButHigherBuy)))
				.contains(new MatchResult(2, 100, new BigDecimal("71000"), 10));
	}

	@Test
	void samePriceChoosesEarlierWaitingOrder() {
		MatchingOrder incomingBuy = order(100, OrderSide.BUY, "70000", 10, "10:00:03Z");
		MatchingOrder earlierSell = order(1, OrderSide.SELL, "70000", 10, "10:00:01Z");
		MatchingOrder laterSell = order(2, OrderSide.SELL, "70000", 10, "10:00:02Z");

		assertThat(matchingEngine.match(incomingBuy, List.of(laterSell, earlierSell)))
				.contains(new MatchResult(100, 1, new BigDecimal("70000"), 10));
	}

	@Test
	void executionPriceIsWaitingOrderPrice() {
		MatchingOrder incomingSell = order(100, OrderSide.SELL, "69000", 10, "10:00:02Z");
		MatchingOrder waitingBuy = order(1, OrderSide.BUY, "71000", 10, "10:00:01Z");

		assertThat(matchingEngine.match(incomingSell, List.of(waitingBuy)))
				.contains(new MatchResult(1, 100, new BigDecimal("71000"), 10));
	}

	@Test
	void onlyOrdersWithExactlySameQuantityCanMatch() {
		MatchingOrder incomingBuy = order(100, OrderSide.BUY, "72000", 10, "10:00:03Z");
		MatchingOrder cheaperWrongQuantity = order(1, OrderSide.SELL, "69000", 5, "10:00:01Z");
		MatchingOrder sameQuantity = order(2, OrderSide.SELL, "70000", 10, "10:00:02Z");

		assertThat(matchingEngine.match(incomingBuy, List.of(cheaperWrongQuantity, sameQuantity)))
				.contains(new MatchResult(100, 2, new BigDecimal("70000"), 10));
	}

	@Test
	void doesNotMatchWhenPricesDoNotCross() {
		MatchingOrder incomingBuy = order(100, OrderSide.BUY, "69000", 10, "10:00:02Z");
		MatchingOrder waitingSell = order(1, OrderSide.SELL, "70000", 10, "10:00:01Z");

		assertThat(matchingEngine.match(incomingBuy, List.of(waitingSell))).isEmpty();
	}

	@Test
	void doesNotMatchDifferentStockOrSameSide() {
		MatchingOrder incomingBuy = order(100, OrderSide.BUY, "72000", 10, "10:00:03Z");
		MatchingOrder differentStock = order(1, "000660", OrderSide.SELL, "70000", 10, "10:00:01Z");
		MatchingOrder sameSide = order(2, OrderSide.BUY, "70000", 10, "10:00:02Z");

		assertThat(matchingEngine.match(incomingBuy, List.of(differentStock, sameSide))).isEmpty();
	}

	private MatchingOrder order(long id, OrderSide side, String price, long quantity, String time) {
		return order(id, "005930", side, price, quantity, time);
	}

	private MatchingOrder order(
			long id,
			String stockCode,
			OrderSide side,
			String price,
			long quantity,
			String time
	) {
		return new MatchingOrder(
				id,
				stockCode,
				side,
				new BigDecimal(price),
				quantity,
				Instant.parse("2026-10-20T" + time)
		);
	}
}
