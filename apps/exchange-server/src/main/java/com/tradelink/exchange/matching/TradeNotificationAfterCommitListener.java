package com.tradelink.exchange.matching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TradeNotificationAfterCommitListener {

	private static final Logger log = LoggerFactory.getLogger(TradeNotificationAfterCommitListener.class);

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void recordPendingNotification(TradeMatchedEvent event) {
		// BrokerClients is intentionally not invoked in STEP 6-3.
		log.debug("Broker notification remains disabled for committed trade {}", event.tradeId());
	}
}
