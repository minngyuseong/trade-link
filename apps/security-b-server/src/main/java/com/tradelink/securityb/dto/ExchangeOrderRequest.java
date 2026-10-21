package com.tradelink.securityb.dto;

public record ExchangeOrderRequest(
        Long userId,
        String brokerCode,
        Long brokerOrderId,
        String stockCode,
        String side,
        Long price,
        Integer quantity
) {}