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
        String status,
        boolean filled,
        LocalDateTime createdAt
) {
}
