package com.tradelink.exchange.matching;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

public class MatchingEngine {

	public Optional<MatchResult> match(MatchingOrder incomingOrder, Collection<MatchingOrder> waitingOrders) {
		return waitingOrders.stream()
				.filter(waitingOrder -> isMatchable(incomingOrder, waitingOrder))
				.min(priorityFor(incomingOrder.side()))
				.map(waitingOrder -> createResult(incomingOrder, waitingOrder));
	}

	private boolean isMatchable(MatchingOrder incomingOrder, MatchingOrder waitingOrder) {
		if (incomingOrder.exchangeOrderId() == waitingOrder.exchangeOrderId()) {
			return false;
		}
		if (!incomingOrder.stockCode().equals(waitingOrder.stockCode())) {
			return false;
		}
		if (incomingOrder.side() == waitingOrder.side()) {
			return false;
		}
		if (incomingOrder.quantity() != waitingOrder.quantity()) {
			return false;
		}

		MatchingOrder buyOrder = incomingOrder.side() == OrderSide.BUY ? incomingOrder : waitingOrder;
		MatchingOrder sellOrder = incomingOrder.side() == OrderSide.SELL ? incomingOrder : waitingOrder;
		return buyOrder.price().compareTo(sellOrder.price()) >= 0;
	}

	private Comparator<MatchingOrder> priorityFor(OrderSide incomingSide) {
		Comparator<MatchingOrder> pricePriority = Comparator.comparing(MatchingOrder::price);
		if (incomingSide == OrderSide.SELL) {
			pricePriority = pricePriority.reversed();
		}

		return pricePriority
				.thenComparing(MatchingOrder::receivedAt)
				.thenComparingLong(MatchingOrder::exchangeOrderId);
	}

	private MatchResult createResult(MatchingOrder incomingOrder, MatchingOrder waitingOrder) {
		MatchingOrder buyOrder = incomingOrder.side() == OrderSide.BUY ? incomingOrder : waitingOrder;
		MatchingOrder sellOrder = incomingOrder.side() == OrderSide.SELL ? incomingOrder : waitingOrder;

		return new MatchResult(
				buyOrder.exchangeOrderId(),
				sellOrder.exchangeOrderId(),
				waitingOrder.price(),
				incomingOrder.quantity()
		);
	}
}
