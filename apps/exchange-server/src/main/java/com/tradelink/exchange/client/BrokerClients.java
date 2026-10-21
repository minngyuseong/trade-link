package com.tradelink.exchange.client;

import java.util.Locale;

import com.tradelink.exchange.config.ExternalClientProperties;
import com.tradelink.exchange.dto.TradeNotice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class BrokerClients {

	private static final Logger log = LoggerFactory.getLogger(BrokerClients.class);
	private static final String TRADE_NOTICE_PATH = "/internal/trades";

	private final RestClient.Builder restClientBuilder;
	private final ExternalClientProperties properties;

	public BrokerClients(RestClient.Builder restClientBuilder, ExternalClientProperties properties) {
		this.restClientBuilder = restClientBuilder;
		this.properties = properties;
	}

	public boolean sendTradeNotice(String brokerCode, TradeNotice notice) {
		if (!StringUtils.hasText(brokerCode)) {
			log.error("Broker code is missing; trade {} was not sent", notice.tradeId());
			return false;
		}

		String normalizedBrokerCode = brokerCode.toUpperCase(Locale.ROOT);
		String baseUrl = properties.getBrokers().entrySet().stream()
				.filter(entry -> entry.getKey().equalsIgnoreCase(normalizedBrokerCode))
				.map(java.util.Map.Entry::getValue)
				.findFirst()
				.orElse("");

		if (!StringUtils.hasText(baseUrl)) {
			log.error("Broker {} URL is not configured; trade {} was not sent", normalizedBrokerCode, notice.tradeId());
			return false;
		}

		try {
			ResponseEntity<Void> response = restClientBuilder.clone()
					.baseUrl(baseUrl)
					.build()
					.post()
					.uri(TRADE_NOTICE_PATH)
					.body(notice)
					.retrieve()
					.toBodilessEntity();
			boolean succeeded = response.getStatusCode().is2xxSuccessful();
			log.info("Broker {} trade notice result: tradeId={}, status={}",
					normalizedBrokerCode, notice.tradeId(), response.getStatusCode());
			return succeeded;
		} catch (RestClientException | IllegalArgumentException exception) {
			log.error("Broker {} trade notice failed: tradeId={}, reason={}",
					normalizedBrokerCode, notice.tradeId(), exception.getMessage());
			return false;
		}
	}
}
