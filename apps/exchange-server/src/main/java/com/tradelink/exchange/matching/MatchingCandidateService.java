package com.tradelink.exchange.matching;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tradelink.krx.dto.OrderDetailsResponse;
import com.tradelink.krx.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class MatchingCandidateService {
	private static final ZoneId DATABASE_ZONE = ZoneId.of("Asia/Seoul");

	private final OrderRepository orderRepository;
	private final MatchingEngine matchingEngine;

	public MatchingCandidateService(OrderRepository orderRepository, MatchingEngine matchingEngine) {
		this.orderRepository = orderRepository;
		this.matchingEngine = matchingEngine;
	}

	public Optional<MatchResult> findNextMatch() {
		return findMatchCandidates().stream().findFirst();
	}

	public List<MatchResult> findMatchCandidates() {
		List<MatchingOrder> remainingOrders = new ArrayList<>(orderRepository.findWaitingOrders().stream()
				.map(this::toMatchingOrder)
				.toList());
		List<MatchResult> candidates = new ArrayList<>();

		boolean foundCandidate;
		do {
			foundCandidate = false;
			for (int incomingIndex = 1; incomingIndex < remainingOrders.size(); incomingIndex++) {
				MatchingOrder incomingOrder = remainingOrders.get(incomingIndex);
				Optional<MatchResult> result = matchingEngine.match(
						incomingOrder,
						remainingOrders.subList(0, incomingIndex)
				);
				if (result.isPresent()) {
					MatchResult candidate = result.orElseThrow();
					candidates.add(candidate);
					remainingOrders.removeIf(order -> order.exchangeOrderId() == candidate.buyOrderId()
							|| order.exchangeOrderId() == candidate.sellOrderId());
					foundCandidate = true;
					break;
				}
			}
		} while (foundCandidate);

		return List.copyOf(candidates);
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
}
