// 각 증권사에게 응답을 보내줌
package com.tradelink.krx.dto;

public record OrderAcceptedResponse(Long exchangeOrderId, String status) {
}
