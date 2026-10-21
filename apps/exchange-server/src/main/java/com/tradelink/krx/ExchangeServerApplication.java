
package com.tradelink.krx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.tradelink")
@EnableScheduling
public class ExchangeServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(
				ExchangeServerApplication.class, args
		);
	}
}
