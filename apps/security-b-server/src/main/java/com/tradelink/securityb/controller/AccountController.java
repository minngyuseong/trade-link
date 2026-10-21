package com.tradelink.securityb.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@CrossOrigin(origins = "*")
@RestController
public class AccountController {

    private final JdbcTemplate jdbcTemplate;

    // application.properties의 default.account-no 값 주입 (기본값: 2001)
    @Value("${default.account-no:2001}")
    private String defaultAccountNo;

    public AccountController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/account")
    public Map<String, Object> getAccount() {
        // 주입받은 기본 계좌의 현재 예수금 조회
        Long cash = jdbcTemplate.queryForObject(
                "SELECT cash FROM accounts WHERE account_no = ?",
                Long.class,
                defaultAccountNo
        );

        // 주입받은 기본 계좌의 보유 주식 목록 조회
        List<Map<String, Object>> rawHoldings = jdbcTemplate.queryForList(
                "SELECT stock_code, quantity FROM holdings WHERE account_no = ?",
                defaultAccountNo
        );

        List<Map<String, Object>> holdings = new ArrayList<>();
        long stockEvaluationTotal = 0;

        for (Map<String, Object> row : rawHoldings) {
            String code = (String) row.get("stock_code");
            int quantity = ((Number) row.get("quantity")).intValue();

            // 삼성전자 목업 단가 400,000원 기준 계산
            long price = "005930".equals(code) ? 400000L : 3000000L;
            long evalAmount = price * quantity;
            stockEvaluationTotal += evalAmount;

            Map<String, Object> h = new HashMap<>();
            h.put("quantity", quantity);
            h.put("averagePrice", price);
            h.put("purchaseAmount", evalAmount);
            h.put("evaluationAmount", evalAmount);
            h.put("profitLoss", 0);
            h.put("profitRate", 0);

            Map<String, Object> stock = new HashMap<>();
            stock.put("code", code);
            stock.put("name", "005930".equals(code) ? "삼성전자" : "SK하이닉스");
            stock.put("price", price);
            h.put("stock", stock);

            holdings.add(h);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", 1);
        result.put("cashBalance", cash != null ? cash : 0L);
        result.put("stockEvaluationAmount", stockEvaluationTotal);
        result.put("totalAsset", (cash != null ? cash : 0L) + stockEvaluationTotal);
        result.put("totalProfitLoss", 0);
        result.put("totalProfitRate", 0);
        result.put("holdings", holdings);

        return result;
    }

    @GetMapping("/account/holdings/{stockCode}")
    public Map<String, Object> getHoldingDetail(@PathVariable String stockCode) {
        // 주입받은 기본 계좌의 특정 종목 보유 수량 조회
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT quantity FROM holdings WHERE account_no = ? AND stock_code = ?",
                defaultAccountNo,
                stockCode
        );

        int quantity = rows.isEmpty() ? 0 : ((Number) rows.get(0).get("quantity")).intValue();

        // 삼성전자 400,000원 / SK하이닉스 3,000,000원 목업 기준 단가
        long price = "005930".equals(stockCode) ? 400000L : 3000000L;
        long evalAmount = price * quantity;

        Map<String, Object> stock = new HashMap<>();
        stock.put("code", stockCode);
        stock.put("name", "005930".equals(stockCode) ? "삼성전자" : "SK하이닉스");
        stock.put("price", price);

        Map<String, Object> result = new HashMap<>();
        result.put("stock", stock);
        result.put("quantity", quantity);
        result.put("averagePrice", price);
        result.put("purchaseAmount", evalAmount);
        result.put("evaluationAmount", evalAmount);
        result.put("profitLoss", 0);
        result.put("profitRate", 0);

        return result;
    }
}