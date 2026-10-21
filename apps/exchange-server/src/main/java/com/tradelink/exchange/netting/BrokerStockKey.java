package com.tradelink.exchange.netting;

public record BrokerStockKey(
		String brokerCode,
		String stockCode
) {
}
