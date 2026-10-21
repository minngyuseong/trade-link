package com.tradelink.exchange.worker;

import com.tradelink.exchange.matching.MatchResult;
import com.tradelink.exchange.matching.MatchingCandidateService;
import com.tradelink.exchange.matching.MatchingTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "matching.worker", name = "enabled", havingValue = "true")
public class MatchingWorker {

	private static final Logger log = LoggerFactory.getLogger(MatchingWorker.class);
	private final MatchingCandidateService matchingCandidateService;
	private final MatchingTransactionService matchingTransactionService;

	public MatchingWorker(MatchingCandidateService matchingCandidateService,
			MatchingTransactionService matchingTransactionService) {
		this.matchingCandidateService = matchingCandidateService;
		this.matchingTransactionService = matchingTransactionService;
	}

	@Scheduled(fixedDelay = 1000)
	public void matchWaitingOrders() {
		for (MatchResult candidate : matchingCandidateService.findMatchCandidates()) {
			try {
				long tradeId = matchingTransactionService.execute(candidate);
				log.info("Matched orders: tradeId={}, buyOrderId={}, sellOrderId={}",
						tradeId, candidate.buyOrderId(), candidate.sellOrderId());
			} catch (RuntimeException exception) {
				log.warn("Matching candidate failed: buyOrderId={}, sellOrderId={}, reason={}",
						candidate.buyOrderId(), candidate.sellOrderId(), exception.getMessage());
			}
		}
	}
}
