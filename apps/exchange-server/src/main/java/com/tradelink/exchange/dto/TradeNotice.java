package com.tradelink.exchange.dto;

import java.math.BigDecimal;

public record TradeNotice(
		long tradeId,
		long brokerOrderId,
		String side,
		String stockCode,
		BigDecimal price,
		long quantity
) {
}
