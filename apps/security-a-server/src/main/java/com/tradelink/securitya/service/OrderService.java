package com.tradelink.securitya.service;

import com.tradelink.securitya.dto.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class OrderService {
    private final JdbcClient jdbc;
    private final RestClient exchange;
    private final String brokerCode;
    private final TradeCallbackService callbackService;

    public OrderService(JdbcClient jdbc,
                        @Qualifier("exchangeClient") RestClient exchange,
                        @Value("${broker.code}") String brokerCode,
                        TradeCallbackService callbackService) {
        this.jdbc = jdbc;
        this.exchange = exchange;
        this.brokerCode = brokerCode;
        this.callbackService = callbackService;
    }

    // ===== 주문 접수 =====
    public OrderResponse create(OrderCreateRequest req) {
        System.out.println("==================================================");
        System.out.println(">>> [1. 프론트 주문 요청 수신] " + req);

        // 1. 증권사 DB에 주문 저장 (상태: PENDING)
        var keyHolder = new GeneratedKeyHolder();
        jdbc.sql("""
                INSERT INTO orders(user_id, account_no, stock_code, side, price, quantity, status)
                VALUES (?, ?, ?, ?, ?, ?, 'PENDING')
                """)
                .param(req.userId())
                .param(req.accountNo())
                .param(req.stockCode())
                .param(req.side())
                .param(req.price())
                .param(req.quantity())
                .update(keyHolder);

        long brokerOrderId = keyHolder.getKey().longValue();

        // 2. 거래소(KRX) DTO 규격에 맞춰 데이터 변환
        // (req의 userId와 price가 String/Integer일 경우를 대비해 Long으로 안전하게 변환)
        Long userId = Long.parseLong(String.valueOf(req.userId()));
        Long price = Long.parseLong(String.valueOf(req.price()));
        Integer quantity = Integer.parseInt(String.valueOf(req.quantity()));

        var exchangeReq = new ExchangeOrderRequest(
                userId,                 // Long userId
                brokerCode,             // String brokerCode ("A")
                brokerOrderId,          // Long brokerOrderId (증권사 주문 PK)
                req.stockCode(),        // String stockCode
                req.side(),             // String side ("BUY" / "SELL")
                price,                  // Long price
                quantity                // Integer quantity
        );

        System.out.println(">>> [2. 거래소 전송 데이터] " + exchangeReq);

        // 3. 거래소(KRX)로 주문 전송 (POST /orders)
        ExchangeOrderAccepted accepted;
        try {
            accepted = exchange.post()
                    .uri("/orders")
                    .body(exchangeReq)
                    .retrieve()
                    .body(ExchangeOrderAccepted.class);

            System.out.println(">>> [3. 거래소 응답 수신] " + accepted);

        } catch (HttpClientErrorException e) {
            System.err.println(">>> [거래소 HTTP 에러 발생] 상태코드: " + e.getStatusCode());
            System.err.println(">>> [거래소 에러 상세 내용]: " + e.getResponseBodyAsString());
            throw e;
        }

        // 4. 거래소 주문번호 반영 및 주문 상태 ACCEPTED로 업데이트
        jdbc.sql("UPDATE orders SET exchange_order_id = ?, status = 'ACCEPTED' WHERE id = ?")
                .param(accepted.exchangeOrderId())
                .param(brokerOrderId)
                .update();

        OrderResponse res = callbackService.findById(brokerOrderId);
        System.out.println(">>> [4. 최종 주문 완료 반환] " + res);
        System.out.println("==================================================");

        return res;
    }
}