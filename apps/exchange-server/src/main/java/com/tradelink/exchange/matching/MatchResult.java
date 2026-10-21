package com.tradelink.exchange.matching;

import java.math.BigDecimal;

public record MatchResult(
		long buyOrderId,
		long sellOrderId,
		BigDecimal price,
		long quantity
) {
}
