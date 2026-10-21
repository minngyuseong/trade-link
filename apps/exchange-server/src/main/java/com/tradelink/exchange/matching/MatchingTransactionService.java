package com.tradelink.exchange.matching;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tradelink.krx.dto.OrderDetailsResponse;
import com.tradelink.krx.repository.OrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchingTransactionService {

	private static final ZoneId DATABASE_ZONE = ZoneId.of("Asia/Seoul");

	private final OrderRepository orderRepository;
	private final TradeRepository tradeRepository;
	private final MatchingEngine matchingEngine;
	private final ApplicationEventPublisher eventPublisher;

	public MatchingTransactionService(OrderRepository orderRepository, TradeRepository tradeRepository,
			MatchingEngine matchingEngine, ApplicationEventPublisher eventPublisher) {
		this.orderRepository = orderRepository;
		this.tradeRepository = tradeRepository;
		this.matchingEngine = matchingEngine;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public long execute(MatchResult candidate) {
		if (candidate.buyOrderId() == candidate.sellOrderId()) {
			throw new IllegalStateException("A single order cannot be matched with itself");
		}

		List<OrderDetailsResponse> lockedOrders = orderRepository.lockWaitingOrders(
				candidate.buyOrderId(), candidate.sellOrderId());
		if (lockedOrders.size() != 2) {
			throw new IllegalStateException("Both orders must still be WAITING");
		}

		Map<Long, OrderDetailsResponse> ordersById = lockedOrders.stream()
				.collect(Collectors.toMap(OrderDetailsResponse::exchangeOrderId, Function.identity()));
		OrderDetailsResponse buyOrder = requireOrder(ordersById, candidate.buyOrderId(), "BUY");
		OrderDetailsResponse sellOrder = requireOrder(ordersById, candidate.sellOrderId(), "SELL");

		MatchResult lockedMatch = rematchLockedOrders(buyOrder, sellOrder)
				.orElseThrow(() -> new IllegalStateException("Locked orders are no longer matchable"));
		if (!sameMatch(candidate, lockedMatch)) {
			throw new IllegalStateException("Locked order values differ from the match candidate");
		}

		long tradeId = tradeRepository.insert(lockedMatch, buyOrder.stockCode());
		int updatedOrders = orderRepository.markMatchedIfWaiting(
				lockedMatch.buyOrderId(), lockedMatch.sellOrderId());
		if (updatedOrders != 2) {
			throw new IllegalStateException("Expected two WAITING orders to become MATCHED");
		}

		eventPublisher.publishEvent(new TradeMatchedEvent(
				tradeId,
				buyOrder.stockCode(),
				lockedMatch.price(),
				lockedMatch.quantity(),
				buyOrder.brokerCode(),
				buyOrder.brokerOrderId(),
				sellOrder.brokerCode(),
				sellOrder.brokerOrderId()
		));
		return tradeId;
	}

	private OrderDetailsResponse requireOrder(Map<Long, OrderDetailsResponse> ordersById,
			long orderId, String expectedSide) {
		OrderDetailsResponse order = ordersById.get(orderId);
		if (order == null || !"WAITING".equals(order.status()) || !expectedSide.equals(order.side())) {
			throw new IllegalStateException("Locked order does not have the expected side and status");
		}
		return order;
	}

	private Optional<MatchResult> rematchLockedOrders(OrderDetailsResponse first,
			OrderDetailsResponse second) {
		List<OrderDetailsResponse> arrivalOrder = List.of(first, second).stream()
				.sorted(Comparator.comparing(OrderDetailsResponse::createdAt)
						.thenComparingLong(OrderDetailsResponse::exchangeOrderId))
				.toList();
		MatchingOrder waitingOrder = toMatchingOrder(arrivalOrder.get(0));
		MatchingOrder incomingOrder = toMatchingOrder(arrivalOrder.get(1));
		return matchingEngine.match(incomingOrder, List.of(waitingOrder));
	}

	private MatchingOrder toMatchingOrder(OrderDetailsResponse order) {
		return new MatchingOrder(
				order.exchangeOrderId(),
				order.stockCode(),
				OrderSide.valueOf(order.side()),
				BigDecimal.valueOf(order.price()),
				order.quantity(),
				order.createdAt().atZone(DATABASE_ZONE).toInstant()
		);
	}

	private boolean sameMatch(MatchResult candidate, MatchResult lockedMatch) {
		return candidate.buyOrderId() == lockedMatch.buyOrderId()
				&& candidate.sellOrderId() == lockedMatch.sellOrderId()
				&& candidate.price().compareTo(lockedMatch.price()) == 0
				&& candidate.quantity() == lockedMatch.quantity();
	}
}
