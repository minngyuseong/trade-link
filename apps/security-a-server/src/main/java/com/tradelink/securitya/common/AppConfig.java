package com.tradelink.securitya.common;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    @Bean("exchangeClient")
    public RestClient exchangeClient(
            @Value("${external.exchange-url}") String baseUrl) {

        System.out.println("==========================================");
        System.out.println(">>> [주입된 거래소 URL]: " + baseUrl);
        System.out.println("==========================================");

        // 연결 및 응답 타임아웃 3초 설정 (무한 멈춤 방지)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(3));

        return RestClient.builder()
                .requestFactory(factory)
                .baseUrl(baseUrl)
                .build();
    }
}