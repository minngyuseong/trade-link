package com.tradelink.exchange.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "external")
public class ExternalClientProperties {

	private Map<String, String> brokers = new HashMap<>();
	private String depositoryUrl = "";

	public Map<String, String> getBrokers() {
		return brokers;
	}

	public void setBrokers(Map<String, String> brokers) {
		this.brokers = brokers;
	}

	public String getDepositoryUrl() {
		return depositoryUrl;
	}

	public void setDepositoryUrl(String depositoryUrl) {
		this.depositoryUrl = depositoryUrl;
	}
}
