package com.tradelink.depository.service;

import com.tradelink.depository.common.BrokerClients;
import com.tradelink.depository.dto.DepositoryDto.SettlementNotice;
import com.tradelink.depository.dto.DepositoryDto.SettlementRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrokerNotifier {

    private final BrokerClients brokers;

    public void notifySettled(SettlementRequest r) {
        // 1. 매수 증권사 통보 전송
        send(normalizeBrokerCode(r.buyBrokerCode()), new SettlementNotice(
                r.tradeId(), r.buyBrokerOrderId(), "BUY", "SETTLED",
                r.stockCode(), r.price(), r.quantity()
        ));

        // 2. 매도 증권사 통보 전송
        send(normalizeBrokerCode(r.sellBrokerCode()), new SettlementNotice(
                r.tradeId(), r.sellBrokerOrderId(), "SELL", "SETTLED",
                r.stockCode(), r.price(), r.quantity()
        ));
    }

    private void send(String brokerCode, SettlementNotice n) {
        try {
            // RestClient 동기식 호출
            brokers.of(brokerCode).post()
                    .uri("/internal/settlements")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(n)
                    .retrieve()
                    .toBodilessEntity();

            // ⭐️ n.orderId() 대신 n.brokerOrderId() 사용
            log.info("[예탁결제원 -> 증권사] 정산 통보 성공: brokerCode={}, brokerOrderId={}, tradeId={}",
                    brokerCode, n.brokerOrderId(), n.tradeId());
        } catch (Exception e) {
            log.warn("[예탁결제원 -> 증권사] 정산 통보 실패: brokerCode={}, tradeId={}, 사유={}",
                    brokerCode, n.tradeId(), e.getMessage());
        }
    }

    private String normalizeBrokerCode(String code) {
        if (code == null) return "";
        String cleanCode = code.trim().toLowerCase();
        return switch (cleanCode) {
            case "broker_a", "a" -> "a";
            case "broker_b", "b" -> "b";
            default -> cleanCode;
        };
    }
}
