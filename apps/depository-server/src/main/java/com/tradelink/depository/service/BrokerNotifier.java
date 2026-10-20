package com.tradelink.depository.service;

import com.tradelink.depository.common.BrokerClients;
import com.tradelink.depository.dto.DepositoryDto.SettlementNotice;
import com.tradelink.depository.dto.DepositoryDto.SettlementRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrokerNotifier {

    private final BrokerClients brokers;

    // 매수 쪽 증권사와 매도 쪽 증권사에 각각 자기 주문 번호로 통보 (v3 구간 4)
    public void notifySettled(SettlementRequest r) {
        send(r.buyBrokerCode(), new SettlementNotice(
                r.tradeId(), r.buyBrokerOrderId(), "BUY", "SETTLED",
                r.stockCode(), r.price(), r.quantity()
        ));

        send(r.sellBrokerCode(), new SettlementNotice(
                r.tradeId(), r.sellBrokerOrderId(), "SELL", "SETTLED",
                r.stockCode(), r.price(), r.quantity()
        ));
    }

    private void send(String brokerCode, SettlementNotice n) {
        try {
            brokers.of(brokerCode).post()
                    .uri("/internal/settlements")
                    .body(n)
                    .retrieve()
                    .toBodilessEntity();
            log.info("[예탁결제원 -> 증권사] 정산 통보 전송 성공: brokerCode={}, tradeId={}", brokerCode, n.tradeId());
        } catch (Exception e) {
            log.warn("[예탁결제원 -> 증권사] 정산 통보 전송 실패 (서버 미기동 등): brokerCode={}, tradeId={}, 사유={}",
                    brokerCode, n.tradeId(), e.getMessage());
        }
    }
}