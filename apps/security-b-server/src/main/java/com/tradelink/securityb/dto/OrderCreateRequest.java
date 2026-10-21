package com.tradelink.securityb.dto;

public record OrderCreateRequest(
        Long userId,
        String stockCode,
        Long price,
        Integer quantity,
        String idempotencyKey,
        String accountNo, // 필드가 없다면 추가, 있다면 유지
        String side       // 필드가 없다면 추가, 있다면 유지
) {
    // 기본값 보정을 위한 생성자 추가
    public OrderCreateRequest {
        if (accountNo == null || accountNo.isBlank()) {
            accountNo = "1001";
        }
        if (side == null || side.isBlank()) {
            side = "BUY";
        }
    }
}