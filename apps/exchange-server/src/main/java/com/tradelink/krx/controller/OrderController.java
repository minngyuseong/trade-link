package com.tradelink.krx.controller;

import com.tradelink.krx.dto.OrderAcceptedResponse;
import com.tradelink.krx.dto.OrderRequest;
import com.tradelink.krx.dto.OrderDetailsResponse;
import com.tradelink.krx.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderAcceptedResponse> accept(@RequestBody OrderRequest request) {
        // [로그 추가] 증권사로부터 들어온 주문 요청 출력
        System.out.println("==================================================");
        System.out.println(">>> [KRX 거래소] 주문 접수 수신: " + request);

        OrderAcceptedResponse response = orderService.accept(request);

        // [로그 추가] 거래소가 증권사에 내려줄 응답 결과 출력
        System.out.println(">>> [KRX 거래소] 주문 접수 완료 응답: " + response);
        System.out.println("==================================================");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{exchangeOrderId}")
    public ResponseEntity<OrderDetailsResponse> getOrder(@PathVariable long exchangeOrderId) {
        return ResponseEntity.ok(orderService.findOrder(exchangeOrderId));
    }
}