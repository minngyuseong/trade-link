package com.tradelink.securityb.dto;

public record SettlementNotice(Long tradeId, Long brokerOrderId, String side, String status,
                               String stockCode, long price, int quantity) {}