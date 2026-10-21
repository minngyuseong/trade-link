package com.tradelink.exchange.client;

import java.util.Optional;

import com.tradelink.exchange.config.ExternalClientProperties;
import com.tradelink.exchange.dto.SettlementRequest;
import com.tradelink.exchange.dto.SettlementResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class KsdClient {

	private static final Logger log = LoggerFactory.getLogger(KsdClient.class);
	private static final String SETTLEMENT_PATH = "/settlements";

	private final RestClient.Builder restClientBuilder;
	private final ExternalClientProperties properties;

	public KsdClient(RestClient.Builder restClientBuilder, ExternalClientProperties properties) {
		this.restClientBuilder = restClientBuilder;
		this.properties = properties;
	}

	public Optional<SettlementResponse> requestSettlement(SettlementRequest request) {
		String baseUrl = properties.getDepositoryUrl();
		if (!StringUtils.hasText(baseUrl)) {
			log.error("Depository URL is not configured; settlement {} was not sent", request.tradeId());
			return Optional.empty();
		}

		try {
			SettlementResponse response = restClientBuilder.clone()
					.baseUrl(baseUrl)
					.build()
					.post()
					.uri(SETTLEMENT_PATH)
					.body(request)
					.retrieve()
					.body(SettlementResponse.class);

			if (response == null) {
				log.error("Depository returned an empty response: tradeId={}", request.tradeId());
				return Optional.empty();
			}

			log.info("Settlement request succeeded: tradeId={}, status={}", response.tradeId(), response.status());
			return Optional.of(response);
		} catch (RestClientException | IllegalArgumentException exception) {
			log.error("Settlement request failed: tradeId={}, reason={}", request.tradeId(), exception.getMessage());
			return Optional.empty();
		}
	}
}
