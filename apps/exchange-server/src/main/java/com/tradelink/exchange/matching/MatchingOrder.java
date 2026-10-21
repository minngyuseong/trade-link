package com.tradelink.exchange.matching;

import java.math.BigDecimal;
import java.time.Instant;

public record MatchingOrder(
		long exchangeOrderId,
		String stockCode,
		OrderSide side,
		BigDecimal price,
		long quantity,
		Instant receivedAt
) {
}
