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
    private final BrokerNotifier brokerNotifier; // ⭐️ 증권사 통보 전용 컴포넌트 주입

    /**
     * 정산 (증권사 단위로 현금과 주식을 이동 후 증권사 A, B로 정산 통보)
     */
    @Transactional
    public boolean settle(SettlementRequest r) {
        long amount = r.price() * r.quantity();

        // 1. 요청 수신 로그
        log.info("📥 [예탁결제원] 정산 요청 수신! tradeId={}, 매수증권사={}, 매도증권사={}, 종목={}, 단가={}원, 수량={}주 (총액: {}원)",
                r.tradeId(), r.buyBrokerCode(), r.sellBrokerCode(), r.stockCode(), r.price(), r.quantity(), amount);

        // 2. 멱등성 검증 (중복 정산 방지)
        Integer done = jdbc.sql("SELECT COUNT(*) FROM settlement WHERE trade_id = :t")
                .param("t", r.tradeId())
                .query(Integer.class)
                .single();

        if (done != null && done > 0) {
            log.warn("⚠️ [예탁결제원] 이미 처리된 체결 ID입니다. 건너뜁니다. tradeId={}", r.tradeId());
            return false;
        }

        // 3. 증권사 잔고 검증
        long buyerCash = getCash(r.buyBrokerCode());
        if (buyerCash < amount) {
            log.error("❌ [예탁결제원] 매수 증권사({}) 현금 부족: 보유={}원, 필요={}원", r.buyBrokerCode(), buyerCash, amount);
            throw new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_CASH", "매수 증권사의 현금이 부족합니다.");
        }

        int sellerStock = getStock(r.sellBrokerCode(), r.stockCode());
        if (sellerStock < r.quantity()) {
            log.error("❌ [예탁결제원] 매도 증권사({}) 주식 부족: 종목={}, 보유={}주, 필요={}주",
                    r.sellBrokerCode(), r.stockCode(), sellerStock, r.quantity());
            throw new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", "매도 증권사의 보유 주식이 부족합니다.");
        }

        // 4. 예탁원 내부 원장 이동 (DVP: 대금 결제 & 주식 인도 동시 실행)
        // 4-1. 현금 이동: 매수 증권사 (-) -> 매도 증권사 (+)
        jdbc.sql("UPDATE broker_cash SET amount = amount - :a WHERE broker_code = :b")
                .param("a", amount).param("b", r.buyBrokerCode()).update();
        jdbc.sql("UPDATE broker_cash SET amount = amount + :a WHERE broker_code = :b")
                .param("a", amount).param("b", r.sellBrokerCode()).update();

        // 4-2. 주식 이동: 매도 증권사 (-) -> 매수 증권사 (+)
        jdbc.sql("UPDATE broker_stock SET quantity = quantity - :q WHERE broker_code = :b AND stock_code = :c")
                .param("q", r.quantity()).param("b", r.sellBrokerCode()).param("c", r.stockCode()).update();

        jdbc.sql("""
                INSERT INTO broker_stock(broker_code, stock_code, quantity) VALUES (:b, :c, :q)
                ON DUPLICATE KEY UPDATE quantity = quantity + :q
                """)
                .param("b", r.buyBrokerCode()).param("c", r.stockCode()).param("q", r.quantity()).update();

        // 5. 정산 기록 저장
        jdbc.sql("INSERT INTO settlement(trade_id, status) VALUES (:t, 'SETTLED')")
                .param("t", r.tradeId()).update();

        log.info("🎉 [예탁결제원] DVP 원장 정산 완료! tradeId={}, 총대금={}원, 수량={}주", r.tradeId(), amount, r.quantity());

        // 6. 증권사 A, B로 정산 완료 통보 전송!
        brokerNotifier.notifySettled(r);

        return true;
    }

    private long getCash(String brokerCode) {
        return jdbc.sql("SELECT amount FROM broker_cash WHERE broker_code = :b")
                .param("b", brokerCode)
                .query(Long.class)
                .optional()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BROKER_NOT_FOUND", "등록되지 않은 증권사입니다."));
    }

    private int getStock(String brokerCode, String stockCode) {
        return jdbc.sql("SELECT quantity FROM broker_stock WHERE broker_code = :b AND stock_code = :c")
                .param("b", brokerCode)
                .param("c", stockCode)
                .query(Integer.class)
                .optional()
                .orElse(0);
    }
}
