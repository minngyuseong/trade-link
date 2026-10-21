package com.tradelink.depository.common;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Component
public class BrokerClients {
    private final Map<String, RestClient> clients = new HashMap<>();

    public BrokerClients(ExternalProperties props) {
        props.getBrokers().forEach((code, url) ->
                clients.put(code, RestClient.builder().baseUrl(url).build()));
    }

    public RestClient of(String brokerCode) {
        RestClient c = clients.get(brokerCode);
        if (c == null) {
            throw new IllegalArgumentException("등록되지 않은 증권사 코드: " + brokerCode);
        }
        return c;
    }
}