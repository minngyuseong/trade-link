// 조회 API 응답에 필요한 DTO
package com.tradelink.krx.dto;

import java.time.LocalDateTime;

public record OrderDetailsResponse(
        Long exchangeOrderId,
        Long userId,
        String brokerCode,
        Long brokerOrderId,
        String stockCode,
        String side,
        Long price,
        Integer quantity,
        Integer remainingQuantity,
        String status,
        boolean filled,
        LocalDateTime createdAt
) {
    public OrderDetailsResponse(Long exchangeOrderId, Long userId, String brokerCode,
            Long brokerOrderId, String stockCode, String side, Long price, Integer quantity,
            String status, boolean filled, LocalDateTime createdAt) {
        this(exchangeOrderId, userId, brokerCode, brokerOrderId, stockCode, side, price,
                quantity, quantity, status, filled, createdAt);
    }
}
