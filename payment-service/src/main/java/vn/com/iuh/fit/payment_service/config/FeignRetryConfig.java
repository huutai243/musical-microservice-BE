package vn.com.iuh.fit.payment_service.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignRetryConfig {
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(1000, 5000, 3); // delay=1s, max=5s, maxAttempts=3
    }
}

