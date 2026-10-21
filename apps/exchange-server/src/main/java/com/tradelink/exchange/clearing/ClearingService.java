package com.tradelink.exchange.clearing;

import java.util.List;

import com.tradelink.exchange.netting.NettingCalculator;
import com.tradelink.exchange.netting.NettingResult;
import com.tradelink.exchange.netting.NettingTrade;
import org.springframework.stereotype.Service;

@Service
public class ClearingService {

	private final NettingCalculator nettingCalculator;

	public ClearingService(NettingCalculator nettingCalculator) {
		this.nettingCalculator = nettingCalculator;
	}

	public ClearingResult createClearing(long clearingId, List<NettingTrade> trades) {
		NettingResult nettingResult = nettingCalculator.calculate(trades);
		if (!nettingResult.isBalanced()) {
			throw new IllegalStateException("Clearing result is not balanced");
		}

		List<Long> tradeIds = trades.stream()
				.map(NettingTrade::tradeId)
				.toList();
		return new ClearingResult(clearingId, tradeIds, nettingResult);
	}
}
