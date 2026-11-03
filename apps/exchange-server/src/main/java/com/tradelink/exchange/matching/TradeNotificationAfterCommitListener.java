package com.tradelink.exchange.matching;

import com.tradelink.exchange.client.BrokerClients;
import com.tradelink.exchange.client.KsdClient;
import com.tradelink.exchange.dto.SettlementRequest;
import com.tradelink.exchange.dto.TradeNotice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TradeNotificationAfterCommitListener {

	private static final Logger log = LoggerFactory.getLogger(TradeNotificationAfterCommitListener.class);
	private final BrokerClients brokerClients;
	private final KsdClient ksdClient;

	public TradeNotificationAfterCommitListener(BrokerClients brokerClients, KsdClient ksdClient) {
		this.brokerClients = brokerClients;
		this.ksdClient = ksdClient;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void recordPendingNotification(TradeMatchedEvent event) {
		boolean buyerNotified = brokerClients.sendTradeNotice(
				event.buyBrokerCode(),
				new TradeNotice(event.tradeId(), event.buyBrokerOrderId(), "BUY",
						event.stockCode(), event.price(), event.quantity()));
		boolean sellerNotified = brokerClients.sendTradeNotice(
				event.sellBrokerCode(),
				new TradeNotice(event.tradeId(), event.sellBrokerOrderId(), "SELL",
						event.stockCode(), event.price(), event.quantity()));

		if (!buyerNotified || !sellerNotified) {
			log.error("Settlement skipped because broker trade notification failed: tradeId={}, buyerNotified={}, sellerNotified={}",
					event.tradeId(), buyerNotified, sellerNotified);
			return;
		}

		SettlementRequest settlementRequest = new SettlementRequest(
				String.valueOf(event.tradeId()),
				event.stockCode(),
				event.price(),
				event.quantity(),
				event.buyBrokerCode(),
				String.valueOf(event.buyBrokerOrderId()),
				event.sellBrokerCode(),
				String.valueOf(event.sellBrokerOrderId())
		);
		boolean settlementRequested = ksdClient.requestSettlement(settlementRequest).isPresent();
		log.info("Trade post-processing completed: tradeId={}, buyerNotified={}, sellerNotified={}, settlementRequested={}",
				event.tradeId(), buyerNotified, sellerNotified, settlementRequested);
	}
}
