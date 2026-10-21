package com.tradelink.securityb.controller;

import com.tradelink.securityb.dto.*;
import com.tradelink.securityb.dto.OrderResponse;
import com.tradelink.securityb.dto.SettlementNotice;
import com.tradelink.securityb.dto.TradeNotice;
import com.tradelink.securityb.service.TradeCallbackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class TradeCallbackController {
    private final TradeCallbackService service;

    public TradeCallbackController(TradeCallbackService service) {
        this.service = service;
    }

    @GetMapping("/orders/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/users/{userId}/orders")
    public List<OrderResponse> getUserOrders(@PathVariable Long userId) {
        return service.findByUser(userId);
    }

    @PostMapping("/internal/trades")
    public void onTrade(@RequestBody TradeNotice notice) {
        service.onTrade(notice);
    }

    @PostMapping("/internal/settlements")
    public void onSettlement(@RequestBody SettlementNotice notice) {
        service.onSettlement(notice);
    }
}