package vn.com.iuh.fit.payment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "vn.com.iuh.fit.payment_service.client")
@EnableScheduling
public class PaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceApplication.class, args);
	}

}
