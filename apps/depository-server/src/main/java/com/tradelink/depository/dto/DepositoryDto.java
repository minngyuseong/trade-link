package com.tradelink.depository.dto;

public class DepositoryDto {

    // 거래소 -> 예탁원 정산 요청 DTO (v3 규격)
    public record SettlementRequest(
            Long tradeId,
            String stockCode,
            long price,
            int quantity,
            String buyBrokerCode,
            Long buyBrokerOrderId,
            String sellBrokerCode,
            Long sellBrokerOrderId
    ) {}

    // 예탁원 -> 거래소 정산 결과 응답 DTO
    public record SettlementResponse(Long tradeId, String status) {}

    // 예탁원 -> 증권사 정산 완료 통보 DTO (v3 규격)
    public record SettlementNotice(
            Long tradeId,
            Long brokerOrderId,
            String side,
            String status,
            String stockCode,
            long price,
            int quantity
    ) {}
}