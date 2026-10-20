package com.tradelink.depository.service;

import com.tradelink.depository.common.ApiException;
import com.tradelink.depository.dto.DepositoryDto.SettlementRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositoryService {

    private final JdbcClient jdbc;

    /**
     * 정산 (증권사 단위로 현금과 주식을 이동)
     * @return 이번에 새로 정산했으면 true, 이미 처리된 건이라 건너뛰었으면 false
     */
    @Transactional
    public boolean settle(SettlementRequest r) {
        // 멱등성 검증 (중복 정산 방지)
        Integer done = jdbc.sql("SELECT COUNT(*) FROM settlement WHERE trade_id = :t")
                .param("t", r.tradeId()).query(Integer.class).single();
        if (done > 0) {
            log.info("[예탁결제원] 이미 처리된 체결 ID입니다. tradeId={}", r.tradeId());
            return false;
        }

        long amount = r.price() * r.quantity();

        // 증권사 단위 총량 검증
        if (getCash(r.buyBrokerCode()) < amount) {
            throw new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_CASH", "매수 증권사의 현금이 부족합니다.");
        }
        if (getStock(r.sellBrokerCode(), r.stockCode()) < r.quantity()) {
            throw new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", "매도 증권사의 보유 주식이 부족합니다.");
        }

        // 증권사 단위 현금 이동: 매수 증권사 -, 매도 증권사 +
        jdbc.sql("UPDATE broker_cash SET amount = amount - :a WHERE broker_code = :b")
                .param("a", amount).param("b", r.buyBrokerCode()).update();
        jdbc.sql("UPDATE broker_cash SET amount = amount + :a WHERE broker_code = :b")
                .param("a", amount).param("b", r.sellBrokerCode()).update();

        // 증권사 단위 주식 이동: 매도 증권사 -, 매수 증권사 +
        jdbc.sql("UPDATE broker_stock SET quantity = quantity - :q WHERE broker_code = :b AND stock_code = :c")
                .param("q", r.quantity()).param("b", r.sellBrokerCode()).param("c", r.stockCode()).update();

        jdbc.sql("""
                INSERT INTO broker_stock(broker_code, stock_code, quantity) VALUES (:b, :c, :q)
                ON DUPLICATE KEY UPDATE quantity = quantity + :q
                """)
                .param("b", r.buyBrokerCode()).param("c", r.stockCode()).param("q", r.quantity()).update();

        // 정산 기록 저장
        jdbc.sql("INSERT INTO settlement(trade_id, status) VALUES (:t, 'SETTLED')")
                .param("t", r.tradeId()).update();

        log.info("[예탁결제원] 정산 완료 - tradeId: {}, 총대금: {}원, 수량: {}주", r.tradeId(), amount, r.quantity());
        return true;
    }

    private long getCash(String brokerCode) {
        return jdbc.sql("SELECT amount FROM broker_cash WHERE broker_code = :b")
                .param("b", brokerCode).query(Long.class).optional()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BROKER_NOT_FOUND", "등록되지 않은 증권사입니다."));
    }

    private int getStock(String brokerCode, String stockCode) {
        return jdbc.sql("SELECT quantity FROM broker_stock WHERE broker_code = :b AND stock_code = :c")
                .param("b", brokerCode).param("c", stockCode).query(Integer.class).optional().orElse(0);
    }
}