package com.tradelink.exchange.matching;

import java.math.BigDecimal;

public record TradeMatchedEvent(
		long tradeId,
		String stockCode,
		BigDecimal price,
		long quantity,
		String buyBrokerCode,
		long buyBrokerOrderId,
		String sellBrokerCode,
		long sellBrokerOrderId
) {
}
