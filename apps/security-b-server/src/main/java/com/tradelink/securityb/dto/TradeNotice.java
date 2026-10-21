package com.tradelink.securityb.dto;

public record TradeNotice(
        Long tradeId,        // 거래소 체결 ID
        Long brokerOrderId,  // 증권사 주문 ID (orders 테이블 id)
        String side,         // "BUY" 또는 "SELL"
        String stockCode,    // 종목코드 (예: "005930")
        long price,          // 체결 단가
        int quantity         // 체결 수량
) {}