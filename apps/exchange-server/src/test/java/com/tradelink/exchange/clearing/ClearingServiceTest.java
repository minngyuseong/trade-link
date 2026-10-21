package com.tradelink.exchange.clearing;

import java.math.BigDecimal;
import java.util.List;

import com.tradelink.exchange.netting.NettingCalculator;
import com.tradelink.exchange.netting.NettingTrade;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClearingServiceTest {

	private final ClearingService clearingService = new ClearingService(new NettingCalculator());

	@Test
	void createsOneClearingResultForMultipleTrades() {
		List<NettingTrade> trades = List.of(
				trade(1, "72000", 10, "A", "B"),
				trade(2, "73000", 4, "B", "A")
		);

		ClearingResult result = clearingService.createClearing(100, trades);

		assertThat(result.clearingId()).isEqualTo(100);
		assertThat(result.tradeIds()).containsExactly(1L, 2L);
		assertThat(result.nettingResult().stockDelta("A", "005930")).isEqualTo(6);
		assertThat(result.nettingResult().cashDelta("A")).isEqualByComparingTo("-428000");
		assertThat(result.nettingResult().stockDelta("B", "005930")).isEqualTo(-6);
		assertThat(result.nettingResult().cashDelta("B")).isEqualByComparingTo("428000");
		assertThat(result.nettingResult().isBalanced()).isTrue();
	}

	@Test
	void preservesClearingIdProvidedByCaller() {
		ClearingResult result = clearingService.createClearing(
				9876,
				List.of(trade(1, "70000", 1, "A", "B"))
		);

		assertThat(result.clearingId()).isEqualTo(9876);
	}

	@Test
	void rejectsDuplicateTradeIdWithinBatch() {
		List<NettingTrade> trades = List.of(
				trade(1, "70000", 1, "A", "B"),
				trade(1, "71000", 1, "B", "A")
		);

		assertThatThrownBy(() -> clearingService.createClearing(100, trades))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Duplicate tradeId: 1");
	}

	@Test
	void createsEmptyBalancedClearingForEmptyTradeList() {
		ClearingResult result = clearingService.createClearing(100, List.of());

		assertThat(result.tradeIds()).isEmpty();
		assertThat(result.nettingResult().stockDeltas()).isEmpty();
		assertThat(result.nettingResult().cashDeltas()).isEmpty();
		assertThat(result.nettingResult().isBalanced()).isTrue();
	}

	private NettingTrade trade(
			long tradeId,
			String price,
			long quantity,
			String buyBrokerCode,
			String sellBrokerCode
	) {
		return new NettingTrade(
				tradeId,
				"005930",
				new BigDecimal(price),
				quantity,
				buyBrokerCode,
				sellBrokerCode
		);
	}
}
