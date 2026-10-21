package com.tradelink.exchange.client;

import java.math.BigDecimal;

import com.tradelink.exchange.config.ExternalClientProperties;
import com.tradelink.exchange.dto.SettlementRequest;
import com.tradelink.exchange.dto.SettlementResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KsdClientTest {

	@Test
	void returnsSettlementResponseOnSuccess() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		ExternalClientProperties properties = new ExternalClientProperties();
		properties.setDepositoryUrl("http://ksd.test");
		KsdClient client = new KsdClient(builder, properties);
		SettlementRequest request = settlementRequest();

		server.expect(requestTo("http://ksd.test/settlements"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(content().json("""
						{
						  "tradeId":"trade-1",
						  "stockCode":"005930",
						  "price":71000,
						  "quantity":3,
						  "buyBrokerCode":"A",
						  "buyBrokerOrderId":"buy-order-1",
						  "sellBrokerCode":"B",
						  "sellBrokerOrderId":"sell-order-1"
						}
						"""))
				.andRespond(withSuccess("""
						{"tradeId":"trade-1","status":"COMPLETED"}
						""", MediaType.APPLICATION_JSON));

		assertThat(client.requestSettlement(request))
				.contains(new SettlementResponse("trade-1", "COMPLETED"));
		server.verify();
	}

	@Test
	void returnsEmptyWhenDepositoryRespondsWithError() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		ExternalClientProperties properties = new ExternalClientProperties();
		properties.setDepositoryUrl("http://ksd.test");
		KsdClient client = new KsdClient(builder, properties);

		server.expect(requestTo("http://ksd.test/settlements"))
				.andRespond(withServerError());

		assertThat(client.requestSettlement(settlementRequest())).isEmpty();
		server.verify();
	}

	private SettlementRequest settlementRequest() {
		return new SettlementRequest(
				"trade-1", "005930", new BigDecimal("71000"), 3L,
				"A", "buy-order-1", "B", "sell-order-1"
		);
	}
}
