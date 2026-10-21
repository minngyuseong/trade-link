package com.tradelink.exchange.client;

import java.math.BigDecimal;

import com.tradelink.exchange.dto.TradeNotice;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Tag("integration")
class BrokerBMockIntegrationTest {

	@Test
	void sendsTradeNoticeToFullApidogMockEndpoint() {
		String mockEndpoint = System.getenv("BROKER_B_MOCK_URL");
		assumeTrue(StringUtils.hasText(mockEndpoint),
				"Set BROKER_B_MOCK_URL to the complete Apidog Mock endpoint");

		TradeNotice notice = new TradeNotice(
				1L,
				1L,
				"BUY",
				"005930",
				new BigDecimal("70000"),
				10L
		);

		ResponseEntity<Void> response = RestClient.create()
				.post()
				.uri(mockEndpoint)
				.body(notice)
				.retrieve()
				.toBodilessEntity();

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
}
