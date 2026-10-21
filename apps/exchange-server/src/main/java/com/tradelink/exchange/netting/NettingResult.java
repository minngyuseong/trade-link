package com.tradelink.exchange.netting;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public record NettingResult(
		Map<BrokerStockKey, Long> stockDeltas,
		Map<String, BigDecimal> cashDeltas
) {

	public NettingResult {
		stockDeltas = Map.copyOf(stockDeltas);
		cashDeltas = Map.copyOf(cashDeltas);
	}

	public long stockDelta(String brokerCode, String stockCode) {
		return stockDeltas.getOrDefault(new BrokerStockKey(brokerCode, stockCode), 0L);
	}

	public BigDecimal cashDelta(String brokerCode) {
		return cashDeltas.getOrDefault(brokerCode, BigDecimal.ZERO);
	}

	public boolean isBalanced() {
		if (cashDeltas.values().stream()
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.compareTo(BigDecimal.ZERO) != 0) {
			return false;
		}

		Map<String, Long> stockTotals = new HashMap<>();
		stockDeltas.forEach((key, delta) -> stockTotals.merge(key.stockCode(), delta, Long::sum));
		return stockTotals.values().stream().allMatch(total -> total == 0L);
	}
}
