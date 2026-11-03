package com.tradelink.depository.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class BrokerClients {
    private final Map<String, RestClient> clients = new HashMap<>();

    public BrokerClients(BrokerConfigProps props) {
        // 초현님 PC IP(172.21.33.33) 기본값 매핑
        Map<String, String> brokerUrls = props.getBrokers();
        if (brokerUrls == null || brokerUrls.isEmpty()) {
            brokerUrls = new HashMap<>();
            brokerUrls.put("a", "http://172.21.33.33:8081");
            brokerUrls.put("b", "http://172.21.33.33:8082");
        } else {
            if (!brokerUrls.containsKey("a") || brokerUrls.get("a").contains("localhost")) {
                brokerUrls.put("a", "http://172.21.33.33:8081");
            }
            if (!brokerUrls.containsKey("b") || brokerUrls.get("b").contains("localhost")) {
                brokerUrls.put("b", "http://172.21.33.33:8082");
            }
        }

        brokerUrls.forEach((code, url) -> {
            RestClient client = RestClient.builder().baseUrl(url).build();
            String upper = code.toUpperCase(Locale.ROOT);
            clients.put(upper, client);
            clients.put(code.toLowerCase(Locale.ROOT), client);
            clients.put("BROKER_" + upper, client);
        });
    }

    public RestClient of(String brokerCode) {
        if (brokerCode == null) {
            throw new IllegalArgumentException("brokerCode가 null입니다.");
        }
        RestClient c = clients.get(brokerCode.trim().toUpperCase(Locale.ROOT));
        if (c == null) {
            c = clients.get(brokerCode.trim().toLowerCase(Locale.ROOT));
        }
        if (c == null) {
            c = clients.get(brokerCode.trim());
        }
        if (c == null) {
            throw new IllegalArgumentException("등록되지 않은 증권사 코드: " + brokerCode);
        }
        return c;
    }

    @Configuration
    @ConfigurationProperties(prefix = "external")
    public static class BrokerConfigProps {
        private Map<String, String> brokers = new HashMap<>();
        public Map<String, String> getBrokers() { return brokers; }
        public void setBrokers(Map<String, String> brokers) { this.brokers = brokers; }
    }
}
