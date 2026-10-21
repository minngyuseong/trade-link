package com.tradelink.exchange.config;

import com.tradelink.exchange.matching.MatchingEngine;
import com.tradelink.exchange.netting.NettingCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CalculationConfig {

	@Bean
	MatchingEngine matchingEngine() {
		return new MatchingEngine();
	}

	@Bean
	NettingCalculator nettingCalculator() {
		return new NettingCalculator();
	}
}
