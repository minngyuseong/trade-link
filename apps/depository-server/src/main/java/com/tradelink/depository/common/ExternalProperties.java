package com.tradelink.depository.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "external")
public class ExternalProperties {
    private Map<String, String> brokers = new HashMap<>();

    public Map<String, String> getBrokers() { return brokers; }
    public void setBrokers(Map<String, String> brokers) { this.brokers = brokers; }
}