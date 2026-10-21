package com.tradelink.securitya.controller;

import com.tradelink.securitya.dto.OrderCreateRequest;
import com.tradelink.securitya.dto.OrderResponse;
import com.tradelink.securitya.service.OrderService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
public class OrderController {
    private final OrderService service;


    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping("/orders")
    public OrderResponse create(@RequestBody OrderCreateRequest req) {
        System.out.println("order 컨트롤러");
        return service.create(req);
    }
}