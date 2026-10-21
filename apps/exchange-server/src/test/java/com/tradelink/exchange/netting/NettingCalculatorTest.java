package com.tradelink.exchange.netting;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NettingCalculatorTest {

	private final NettingCalculator calculator = new NettingCalculator();

	@Test
	void calculatesRequiredTwoTradeScenario() {
		NettingTrade firstTrade = trade(1, "005930", "72000", 10, "A", "B");
		NettingTrade secondTrade = trade(2, "005930", "73000", 4, "B", "A");

		NettingResult result = calculator.calculate(List.of(firstTrade, secondTrade));

		assertThat(result.stockDelta("A", "005930")).isEqualTo(6);
		assertThat(result.cashDelta("A")).isEqualByComparingTo("-428000");
		assertThat(result.stockDelta("B", "005930")).isEqualTo(-6);
		assertThat(result.cashDelta("B")).isEqualByComparingTo("428000");
		assertThat(result.isBalanced()).isTrue();
	}

	@Test
	void aggregatesStocksSeparatelyForMultipleStockCodes() {
		NettingTrade samsung = trade(1, "005930", "70000", 10, "A", "B");
		NettingTrade hynix = trade(2, "000660", "120000", 2, "A", "C");

		NettingResult result = calculator.calculate(List.of(samsung, hynix));

		assertThat(result.stockDelta("A", "005930")).isEqualTo(10);
		assertThat(result.stockDelta("B", "005930")).isEqualTo(-10);
		assertThat(result.stockDelta("A", "000660")).isEqualTo(2);
		assertThat(result.stockDelta("C", "000660")).isEqualTo(-2);
		assertThat(result.cashDelta("A")).isEqualByComparingTo("-940000");
		assertThat(result.cashDelta("B")).isEqualByComparingTo("700000");
		assertThat(result.cashDelta("C")).isEqualByComparingTo("240000");
		assertThat(result.isBalanced()).isTrue();
	}

	@Test
	void rejectsDuplicateTradeId() {
		NettingTrade first = trade(1, "005930", "70000", 10, "A", "B");
		NettingTrade duplicate = trade(1, "000660", "120000", 2, "B", "A");

		assertThatThrownBy(() -> calculator.calculate(List.of(first, duplicate)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Duplicate tradeId: 1");
	}

	@Test
	void emptyTradeListProducesEmptyBalancedResult() {
		NettingResult result = calculator.calculate(List.of());

		assertThat(result.stockDeltas()).isEmpty();
		assertThat(result.cashDeltas()).isEmpty();
		assertThat(result.isBalanced()).isTrue();
	}

	@Test
	void tradeWithinSameBrokerNetsToZero() {
		NettingTrade internalTrade = trade(1, "005930", "70000", 10, "A", "A");

		NettingResult result = calculator.calculate(List.of(internalTrade));

		assertThat(result.stockDelta("A", "005930")).isZero();
		assertThat(result.cashDelta("A")).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(result.isBalanced()).isTrue();
	}

	private NettingTrade trade(
			long tradeId,
			String stockCode,
			String price,
			long quantity,
			String buyBrokerCode,
			String sellBrokerCode
	) {
		return new NettingTrade(
				tradeId,
				stockCode,
				new BigDecimal(price),
				quantity,
				buyBrokerCode,
				sellBrokerCode
		);
	}
}
