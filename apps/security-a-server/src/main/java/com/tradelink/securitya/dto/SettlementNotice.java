package com.tradelink.securitya.dto;

public record SettlementNotice(Long tradeId, Long brokerOrderId, String side, String status,
                               String stockCode, long price, int quantity) {}