// 값을 검증
package com.tradelink.krx.service;

import com.tradelink.krx.dto.OrderAcceptedResponse;
import com.tradelink.krx.dto.OrderRequest;
import com.tradelink.krx.dto.OrderDetailsResponse;
import com.tradelink.krx.repository.OrderRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderAcceptedResponse accept(OrderRequest request) {
        validate(request);
        try {
            long id = orderRepository.insertWaiting(request);
            return new OrderAcceptedResponse(id, "WAITING");
        } catch (DuplicateKeyException exception) {
            OrderDetailsResponse existing = orderRepository.findByBrokerOrderId(
                    request.brokerCode(), request.brokerOrderId());
            if (isSameOrder(request, existing)) {
                return new OrderAcceptedResponse(existing.exchangeOrderId(), existing.status());
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This broker order ID has already been used for a different order");
        }
    }

    private boolean isSameOrder(OrderRequest request, OrderDetailsResponse existing) {
        return existing != null
                && request.userId().equals(existing.userId())
                && request.brokerCode().equals(existing.brokerCode())
                && request.brokerOrderId().equals(existing.brokerOrderId())
                && request.stockCode().equals(existing.stockCode())
                && request.side().equals(existing.side())
                && request.price().equals(existing.price())
                && request.quantity().equals(existing.quantity());
    }

    public OrderDetailsResponse findOrder(long exchangeOrderId) {
        if (exchangeOrderId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }

        OrderDetailsResponse order = orderRepository.findByExchangeOrderId(exchangeOrderId);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        return order;
    }

    private void validate(OrderRequest request) {
        if (request == null
                || request.userId() == null || request.userId() <= 0
                || request.side() == null
                || !(request.side().equals("BUY") || request.side().equals("SELL"))
                || request.quantity() == null || request.quantity() <= 0
                || request.price() == null || request.price() <= 0
                || request.brokerCode() == null
                || !(request.brokerCode().equals("A") || request.brokerCode().equals("B"))
                || request.brokerOrderId() == null || request.brokerOrderId() <= 0
                || request.stockCode() == null
                || !(request.stockCode().equals("005930") || request.stockCode().equals("000660"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order request");
        }
    }
}
