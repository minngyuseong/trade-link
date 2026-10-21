// 요청 JSON을 담는 DTO
package com.tradelink.krx.dto;

public record OrderRequest(
        Long userId,
        String side,
        Integer quantity,
        Long price,
        String brokerCode,
        Long brokerOrderId,
        String stockCode
) {
}
