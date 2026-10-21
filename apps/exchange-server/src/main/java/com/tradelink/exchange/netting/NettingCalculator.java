package com.tradelink.exchange.netting;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NettingCalculator {

	public NettingResult calculate(Collection<NettingTrade> trades) {
		Set<Long> processedTradeIds = new HashSet<>();
		Map<BrokerStockKey, Long> stockDeltas = new HashMap<>();
		Map<String, BigDecimal> cashDeltas = new HashMap<>();

		for (NettingTrade trade : trades) {
			if (!processedTradeIds.add(trade.tradeId())) {
				throw new IllegalArgumentException("Duplicate tradeId: " + trade.tradeId());
			}

			BigDecimal tradeAmount = trade.price().multiply(BigDecimal.valueOf(trade.quantity()));

			stockDeltas.merge(
					new BrokerStockKey(trade.buyBrokerCode(), trade.stockCode()),
					trade.quantity(),
					Long::sum
			);
			stockDeltas.merge(
					new BrokerStockKey(trade.sellBrokerCode(), trade.stockCode()),
					-trade.quantity(),
					Long::sum
			);

			cashDeltas.merge(trade.buyBrokerCode(), tradeAmount.negate(), BigDecimal::add);
			cashDeltas.merge(trade.sellBrokerCode(), tradeAmount, BigDecimal::add);
		}

		NettingResult result = new NettingResult(stockDeltas, cashDeltas);
		if (!result.isBalanced()) {
			throw new IllegalStateException("Netting result is not balanced");
		}
		return result;
	}
}
