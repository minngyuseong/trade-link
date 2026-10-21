package com.tradelink.securityb.controller;

import com.tradelink.securityb.dto.OrderCreateRequest;
import com.tradelink.securityb.dto.OrderResponse;
import com.tradelink.securityb.service.OrderService;
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