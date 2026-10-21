package com.tradelink.exchange.worker;

import java.math.BigDecimal;
import java.util.List;

import com.tradelink.exchange.matching.MatchResult;
import com.tradelink.exchange.matching.MatchingCandidateService;
import com.tradelink.exchange.matching.MatchingTransactionService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MatchingWorkerTest {

	private final MatchingCandidateService candidateService = mock(MatchingCandidateService.class);
	private final MatchingTransactionService transactionService = mock(MatchingTransactionService.class);
	private final MatchingWorker worker = new MatchingWorker(candidateService, transactionService);

	@Test
	void doesNothingWhenThereIsNoCandidate() {
		when(candidateService.findMatchCandidates()).thenReturn(List.of());

		worker.matchWaitingOrders();

		verify(transactionService, never()).execute(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void executesEveryCandidate() {
		MatchResult first = match(1, 2);
		MatchResult second = match(3, 4);
		when(candidateService.findMatchCandidates()).thenReturn(List.of(first, second));
		when(transactionService.execute(first)).thenReturn(101L);
		when(transactionService.execute(second)).thenReturn(102L);

		worker.matchWaitingOrders();

		var ordered = inOrder(transactionService);
		ordered.verify(transactionService).execute(first);
		ordered.verify(transactionService).execute(second);
	}

	@Test
	void continuesWithNextCandidateWhenOneFails() {
		MatchResult first = match(1, 2);
		MatchResult second = match(3, 4);
		when(candidateService.findMatchCandidates()).thenReturn(List.of(first, second));
		when(transactionService.execute(first))
				.thenThrow(new IllegalStateException("locked by another transaction"));
		when(transactionService.execute(second)).thenReturn(102L);

		worker.matchWaitingOrders();

		verify(transactionService).execute(first);
		verify(transactionService).execute(second);
	}

	private MatchResult match(long buyOrderId, long sellOrderId) {
		return new MatchResult(buyOrderId, sellOrderId, BigDecimal.valueOf(70_000), 10);
	}
}
