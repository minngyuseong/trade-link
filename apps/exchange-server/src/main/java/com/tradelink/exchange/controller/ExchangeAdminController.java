package com.tradelink.exchange.controller;

import java.util.List;

import com.tradelink.exchange.matching.TradeRepository;
import com.tradelink.exchange.matching.TradeRepository.TradeView;
import com.tradelink.krx.dto.OrderDetailsResponse;
import com.tradelink.krx.repository.OrderRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/admin")
public class ExchangeAdminController {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;

    public ExchangeAdminController(OrderRepository orderRepository, TradeRepository tradeRepository) {
        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;
    }

    @GetMapping("/orders")
    public List<OrderDetailsResponse> orders() {
        return orderRepository.findRecentOrders();
    }

    @GetMapping("/trades")
    public List<TradeView> trades() {
        return tradeRepository.findRecentTrades();
    }
}
