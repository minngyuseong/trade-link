package com.tradelink.exchange.client;

import java.math.BigDecimal;
import java.util.Map;

import com.tradelink.exchange.config.ExternalClientProperties;
import com.tradelink.exchange.dto.TradeNotice;
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

class BrokerClientsTest {

	@Test
	void sendsTradeNoticeToBrokerA() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		ExternalClientProperties properties = new ExternalClientProperties();
		properties.setBrokers(Map.of(
				"A", "http://broker-a.test",
				"B", "http://broker-b.test"
		));
		BrokerClients clients = new BrokerClients(builder, properties);
		TradeNotice notice = new TradeNotice(
				1L, 1L, "BUY", "005930", new BigDecimal("71000"), 3L
		);

		server.expect(requestTo("http://broker-a.test/internal/trades"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(content().json("""
						{"tradeId":1,"brokerOrderId":1,"side":"BUY","stockCode":"005930","price":71000,"quantity":3}
						"""))
				.andRespond(withSuccess());

		assertThat(clients.sendTradeNotice("a", notice)).isTrue();
		server.verify();
	}

	@Test
	void sendsTradeNoticeToBrokerB() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		ExternalClientProperties properties = new ExternalClientProperties();
		properties.setBrokers(Map.of(
				"A", "http://broker-a.test",
				"B", "http://broker-b.test"
		));
		BrokerClients clients = new BrokerClients(builder, properties);
		TradeNotice notice = new TradeNotice(
				2L, 20L, "SELL", "000660", new BigDecimal("130000"), 5L
		);

		server.expect(requestTo("http://broker-b.test/internal/trades"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(content().json("""
						{"tradeId":2,"brokerOrderId":20,"side":"SELL","stockCode":"000660","price":130000,"quantity":5}
						"""))
				.andRespond(withSuccess());

		assertThat(clients.sendTradeNotice("B", notice)).isTrue();
		server.verify();
	}

	@Test
	void returnsFalseWhenBrokerUrlIsMissing() {
		BrokerClients clients = new BrokerClients(RestClient.builder(), new ExternalClientProperties());
		TradeNotice notice = new TradeNotice(
				1L, 1L, "BUY", "005930", new BigDecimal("71000"), 3L
		);

		assertThat(clients.sendTradeNotice("A", notice)).isFalse();
	}

	@Test
	void returnsFalseWhenBrokerRespondsWithError() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		ExternalClientProperties properties = new ExternalClientProperties();
		properties.setBrokers(Map.of("A", "http://broker-a.test"));
		BrokerClients clients = new BrokerClients(builder, properties);
		TradeNotice notice = new TradeNotice(
				1L, 1L, "BUY", "005930", new BigDecimal("71000"), 3L
		);

		server.expect(requestTo("http://broker-a.test/internal/trades"))
				.andRespond(withServerError());

		assertThat(clients.sendTradeNotice("A", notice)).isFalse();
		server.verify();
	}
}
