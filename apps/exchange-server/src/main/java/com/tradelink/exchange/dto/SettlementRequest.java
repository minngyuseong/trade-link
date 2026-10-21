package com.tradelink.exchange.dto;

import java.math.BigDecimal;

public record SettlementRequest(
		String tradeId,
		String stockCode,
		BigDecimal price,
		long quantity,
		String buyBrokerCode,
		String buyBrokerOrderId,
		String sellBrokerCode,
		String sellBrokerOrderId
) {
}
