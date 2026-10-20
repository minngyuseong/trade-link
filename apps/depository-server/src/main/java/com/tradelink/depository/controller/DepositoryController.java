package com.tradelink.depository.controller;

import com.tradelink.depository.dto.DepositoryDto.SettlementRequest;
import com.tradelink.depository.dto.DepositoryDto.SettlementResponse;
import com.tradelink.depository.service.BrokerNotifier;
import com.tradelink.depository.service.DepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Depository API", description = "한국예탁결제원 체결 정산 관리")
@RestController
@RequiredArgsConstructor
public class DepositoryController {

    private final DepositoryService service;
    private final BrokerNotifier notifier;

    @Operation(summary = "체결 정산 실행 (한국거래소 호출)", description = "증권사 단위 현금/주식 이체 후 양 증권사로 정산 완료 통보")
    @PostMapping("/settlements")
    public SettlementResponse settle(@RequestBody SettlementRequest req) {
        boolean newlySettled = service.settle(req);
        if (newlySettled) {
            notifier.notifySettled(req);
        }
        return new SettlementResponse(req.tradeId(), "SETTLED");
    }
}