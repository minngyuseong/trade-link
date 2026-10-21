package com.tradelink.exchange.matching;

import java.time.LocalDateTime;
import java.util.List;

import com.tradelink.krx.dto.OrderDetailsResponse;
import com.tradelink.krx.repository.OrderRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MatchingCandidateServiceTest {

	private final OrderRepository orderRepository = mock(OrderRepository.class);
	private final MatchingCandidateService service =
			new MatchingCandidateService(orderRepository, new MatchingEngine());

	@Test
	void findsMatchFromWaitingOrdersInArrivalOrder() {
		when(orderRepository.findWaitingOrders()).thenReturn(List.of(
				order(1, "A", 101, "BUY", 70_000, LocalDateTime.of(2026, 1, 1, 9, 0)),
				order(2, "B", 201, "SELL", 69_000, LocalDateTime.of(2026, 1, 1, 9, 1))
		));

		MatchResult result = service.findNextMatch().orElseThrow();

		assertThat(result.buyOrderId()).isEqualTo(1);
		assertThat(result.sellOrderId()).isEqualTo(2);
		assertThat(result.price()).isEqualByComparingTo("70000");
		assertThat(result.quantity()).isEqualTo(10);
	}

	@Test
	void returnsEmptyWhenWaitingOrdersDoNotCross() {
		when(orderRepository.findWaitingOrders()).thenReturn(List.of(
				order(1, "A", 101, "BUY", 68_000, LocalDateTime.of(2026, 1, 1, 9, 0)),
				order(2, "B", 201, "SELL", 69_000, LocalDateTime.of(2026, 1, 1, 9, 1))
		));

		assertThat(service.findNextMatch()).isEmpty();
	}

	@Test
	void returnsMultipleCandidatesWithoutReusingAnOrder() {
		when(orderRepository.findWaitingOrders()).thenReturn(List.of(
				order(1, "A", 101, "BUY", 70_000, LocalDateTime.of(2026, 1, 1, 9, 0)),
				order(2, "B", 201, "SELL", 69_000, LocalDateTime.of(2026, 1, 1, 9, 1)),
				order(3, "A", 102, "BUY", 80_000, LocalDateTime.of(2026, 1, 1, 9, 2)),
				order(4, "B", 202, "SELL", 79_000, LocalDateTime.of(2026, 1, 1, 9, 3))
		));

		assertThat(service.findMatchCandidates())
				.extracting(MatchResult::buyOrderId, MatchResult::sellOrderId)
				.containsExactly(
						org.assertj.core.groups.Tuple.tuple(1L, 2L),
						org.assertj.core.groups.Tuple.tuple(3L, 4L)
				);
	}

	private OrderDetailsResponse order(long id, String brokerCode, long brokerOrderId,
			String side, long price, LocalDateTime createdAt) {
		return new OrderDetailsResponse(
				id, 1L, brokerCode, brokerOrderId, "005930", side,
				price, 10, "WAITING", false, createdAt
		);
	}
}
