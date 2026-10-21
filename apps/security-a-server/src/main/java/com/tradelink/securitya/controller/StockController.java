package com.tradelink.securitya.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/stocks")
public class StockController {

    @GetMapping
    public List<Map<String, Object>> getStocks() {
        System.out.println("stocks 호출됨");
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> samsung = new HashMap<>();
        samsung.put("code", "005930");
        samsung.put("name", "삼성전자");
        samsung.put("price", 400000L);
        samsung.put("changeRate", 0.0);       // 👈 toFixed() 호출용 등락률
        samsung.put("changeAmount", 0L);      // 👈 변동 금액
        samsung.put("previousClose", 400000L);
        list.add(samsung);

        Map<String, Object> hynix = new HashMap<>();
        hynix.put("code", "000660");
        hynix.put("name", "SK하이닉스");
        hynix.put("price", 3000000L);
        hynix.put("changeRate", 0.0);         // 👈 toFixed() 호출용 등락률
        hynix.put("changeAmount", 0L);        // 👈 변동 금액
        hynix.put("previousClose", 3000000L);
        list.add(hynix);

        return list;
    }

    @GetMapping("/{stockCode}")
    public Map<String, Object> getStock(@PathVariable String stockCode) {
        Map<String, Object> stock = new HashMap<>();
        stock.put("code", stockCode);

        if ("005930".equals(stockCode)) {
            stock.put("name", "삼성전자");
            stock.put("price", 400000L);
            stock.put("changeRate", 0.0);
            stock.put("changeAmount", 0L);
            stock.put("previousClose", 400000L);
        } else {
            stock.put("name", "SK하이닉스");
            stock.put("price", 3000000L);
            stock.put("changeRate", 0.0);
            stock.put("changeAmount", 0L);
            stock.put("previousClose", 3000000L);
        }

        return stock;
    }
}