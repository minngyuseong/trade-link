package com.tradelink.exchange.clearing;

import java.util.List;

import com.tradelink.exchange.netting.NettingResult;

public record ClearingResult(
		long clearingId,
		List<Long> tradeIds,
		NettingResult nettingResult
) {

	public ClearingResult {
		tradeIds = List.copyOf(tradeIds);
	}
}
