package com.tradelink.securitya.service;

import com.tradelink.securitya.common.ApiException;
import com.tradelink.securitya.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TradeCallbackService {
    private final JdbcClient jdbc;

    public TradeCallbackService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // [조회 API] 단건 조회
    public OrderResponse findById(Long id) {
        return jdbc.sql("""
            SELECT o.*, CAST(o.id AS CHAR) AS order_id
            FROM orders o
            WHERE o.id = :id
            """)
                .param("id", id)
                .query(OrderResponse.class)
                .optional()
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "ORDER_NOT_FOUND",
                        "없는 주문입니다."
                ));
    }

    // [조회 API] 유저별 주문 목록 조회 (React 화면 연동용)
    public List<OrderResponse> findByUser(Long userId) {
        return jdbc.sql("""
            SELECT o.*, CAST(o.id AS CHAR) AS order_id
            FROM orders o
            WHERE o.user_id = :u
            ORDER BY o.id DESC
            """)
                .param("u", userId)
                .query(OrderResponse.class)
                .list();
    }

    // 3번 API: 거래소 체결 통보 처리
    @Transactional
    public void onTrade(TradeNotice n) {
        int updated = jdbc.sql("""
                UPDATE orders 
                SET status = 'MATCHED', trade_id = :t 
                WHERE id = :id AND status = 'PENDING'
                """)
                .param("t", n.tradeId())
                .param("id", n.brokerOrderId())
                .update();

        if (updated > 0) {
            System.out.println("✅ [3번 체결 성공] 주문 #" + n.brokerOrderId() + " -> MATCHED (거래소 체결ID: " + n.tradeId() + ")");
        } else {
            System.out.println("⚠️ [3번 체결 스킵] 주문 #" + n.brokerOrderId() + " (대기 중인 주문이 아니거나 이미 처리됨)");
        }
    }

    // 4번 API: 예탁원 정산 완료 통보 처리
    @Transactional
    public void onSettlement(SettlementNotice n) {
        if (!"SETTLED".equals(n.status())) return;

        int changed = jdbc.sql("UPDATE orders SET status = 'SETTLED' WHERE id = :id AND status = 'MATCHED'")
                .param("id", n.brokerOrderId()).update();
        if (changed == 0) return;

        // DB에서 원본 주문 내역 조회
        OrderResponse o = findById(n.brokerOrderId());
        long amount = o.price() * o.quantity();

        if ("BUY".equalsIgnoreCase(o.side())) {
            // 매수: 예수금 차감, 보유주식 증가
            jdbc.sql("UPDATE accounts SET cash = cash - :a WHERE account_no = :no")
                    .param("a", amount).param("no", o.accountNo()).update();
            jdbc.sql("""
                    INSERT INTO holdings(account_no, stock_code, quantity) VALUES (:no, :c, :q)
                    ON DUPLICATE KEY UPDATE quantity = quantity + :q
                    """)
                    .param("no", o.accountNo()).param("c", o.stockCode()).param("q", o.quantity()).update();
        } else {
            // 매도: 예수금 증가, 보유주식 차감
            jdbc.sql("UPDATE accounts SET cash = cash + :a WHERE account_no = :no")
                    .param("a", amount).param("no", o.accountNo()).update();
            jdbc.sql("UPDATE holdings SET quantity = quantity - :q WHERE account_no = :no AND stock_code = :c")
                    .param("q", o.quantity()).param("no", o.accountNo()).param("c", o.stockCode()).update();
        }

        System.out.println("🎉 [4번 정산 완료] 주문 #" + o.id() + " -> SETTLED 및 계좌/보유주식 반영 완료");
    }
}