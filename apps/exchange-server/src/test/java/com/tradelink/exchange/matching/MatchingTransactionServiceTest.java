package com.tradelink.exchange.matching;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.tradelink.krx.dto.OrderDetailsResponse;
import com.tradelink.krx.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MatchingTransactionServiceTest {

	private final OrderRepository orderRepository = mock(OrderRepository.class);
	private final TradeRepository tradeRepository = mock(TradeRepository.class);
	private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
	private MatchingTransactionService service;

	@BeforeEach
	void setUp() {
		service = new MatchingTransactionService(
				orderRepository, tradeRepository, new MatchingEngine(), eventPublisher);
	}

	@Test
	void storesTradeAndMarksBothOrdersMatched() {
		MatchResult candidate = candidate();
		when(orderRepository.lockWaitingOrders(1, 2)).thenReturn(waitingPair(70_000, 69_000));
		when(tradeRepository.insert(candidate, "005930")).thenReturn(99L);
		when(orderRepository.markMatchedIfWaiting(1, 2)).thenReturn(2);

		assertThat(service.execute(candidate)).isEqualTo(99L);

		verify(tradeRepository).insert(candidate, "005930");
		verify(orderRepository).markMatchedIfWaiting(1, 2);
		verify(eventPublisher).publishEvent(any(TradeMatchedEvent.class));
	}

	@Test
	void rejectsOrderThatIsAlreadyMatched() {
		when(orderRepository.lockWaitingOrders(1, 2)).thenReturn(List.of(buy("WAITING", 70_000)));

		assertThatThrownBy(() -> service.execute(candidate()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("WAITING");

		verify(tradeRepository, never()).insert(any(), any());
	}

	@Test
	void rejectsOrdersWhoseValuesChangedAfterCandidateSelection() {
		when(orderRepository.lockWaitingOrders(1, 2)).thenReturn(waitingPair(70_000, 71_000));

		assertThatThrownBy(() -> service.execute(candidate()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("no longer matchable");

		verify(tradeRepository, never()).insert(any(), any());
	}

	@Test
	void propagatesTradeInsertFailureBeforeChangingOrderStatus() {
		when(orderRepository.lockWaitingOrders(1, 2)).thenReturn(waitingPair(70_000, 69_000));
		when(tradeRepository.insert(candidate(), "005930"))
				.thenThrow(new IllegalStateException("trade insert failed"));

		assertThatThrownBy(() -> service.execute(candidate()))
				.hasMessage("trade insert failed");

		verify(orderRepository, never()).markMatchedIfWaiting(1, 2);
		verify(eventPublisher, never()).publishEvent(any());
	}

	@Test
	void rejectsPartialStatusUpdateSoTransactionCanRollBack() {
		when(orderRepository.lockWaitingOrders(1, 2)).thenReturn(waitingPair(70_000, 69_000));
		when(tradeRepository.insert(candidate(), "005930")).thenReturn(99L);
		when(orderRepository.markMatchedIfWaiting(1, 2)).thenReturn(1);

		assertThatThrownBy(() -> service.execute(candidate()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("two WAITING orders");

		verify(eventPublisher, never()).publishEvent(any());
	}

	@Test
	void rejectsSecondAttemptToMatchTheSameOrderPair() {
		MatchResult candidate = candidate();
		when(orderRepository.lockWaitingOrders(1, 2))
				.thenReturn(waitingPair(70_000, 69_000))
				.thenReturn(List.of());
		when(tradeRepository.insert(candidate, "005930")).thenReturn(99L);
		when(orderRepository.markMatchedIfWaiting(1, 2)).thenReturn(2);

		assertThat(service.execute(candidate)).isEqualTo(99L);
		assertThatThrownBy(() -> service.execute(candidate))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("WAITING");

		verify(tradeRepository, times(1)).insert(candidate, "005930");
		verify(orderRepository, times(1)).markMatchedIfWaiting(1, 2);
		verify(eventPublisher, times(1)).publishEvent(any(TradeMatchedEvent.class));
	}

	private MatchResult candidate() {
		return new MatchResult(1, 2, BigDecimal.valueOf(70_000), 10);
	}

	private List<OrderDetailsResponse> waitingPair(long buyPrice, long sellPrice) {
		return List.of(
				buy("WAITING", buyPrice),
				order(2, "B", 201, "SELL", sellPrice, "WAITING",
						LocalDateTime.of(2026, 1, 1, 9, 1))
		);
	}

	private OrderDetailsResponse buy(String status, long price) {
		return order(1, "A", 101, "BUY", price, status,
				LocalDateTime.of(2026, 1, 1, 9, 0));
	}

	private OrderDetailsResponse order(long id, String brokerCode, long brokerOrderId,
			String side, long price, String status, LocalDateTime createdAt) {
		return new OrderDetailsResponse(
				id, 1L, brokerCode, brokerOrderId, "005930", side,
				price, 10, status, "MATCHED".equals(status), createdAt
		);
	}
}
