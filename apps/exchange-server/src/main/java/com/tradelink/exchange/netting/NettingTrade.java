package com.tradelink.exchange.netting;

import java.math.BigDecimal;

public record NettingTrade(
		long tradeId,
		String stockCode,
		BigDecimal price,
		long quantity,
		String buyBrokerCode,
		String sellBrokerCode
) {
}
