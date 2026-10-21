package com.tradelink.securityb.dto;

public record OrderResponse(
        Long id,
        String orderId, // 프론트 slice용 필드 추가
        Long userId,
        String accountNo,
        String stockCode,
        String side,
        Long price,
        Integer quantity,
        String status,
        Long exchangeOrderId,
        Long tradeId
) {
    // id만 있을 때 orderId를 자동 변환해 주는 편의 생성자 (선택 사항)
    public OrderResponse(Long id, Long userId, String accountNo, String stockCode,
                         String side, Long price, Integer quantity, String status,
                         Long exchangeOrderId, Long tradeId) {
        this(id, String.valueOf(id), userId, accountNo, stockCode, side, price, quantity, status, exchangeOrderId, tradeId);
    }
}