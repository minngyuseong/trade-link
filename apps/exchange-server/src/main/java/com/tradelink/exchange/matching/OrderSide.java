package com.tradelink.exchange.matching;

public enum OrderSide {
	BUY,
	SELL;

	public OrderSide opposite() {
		return this == BUY ? SELL : BUY;
	}
}
