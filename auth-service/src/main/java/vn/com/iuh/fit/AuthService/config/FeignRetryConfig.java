package vn.com.iuh.fit.AuthService.config;

import feign.RetryableException;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

@Configuration
public class FeignRetryConfig {

    @Bean
    public Retryer retryer() {
        // delayStart = 1 s, maxDelay = 5 s, maxAttempts = 3
        return new Retryer.Default(1000, 5000, 3);
    }

    /**  HTTP 5xxRetryableException run Retryer */
    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            if (response.status() >= 500) {
                return new RetryableException(
                        response.status(),                 // 1) status 5xx
                        "Retry for 5xx",                   // 2) message
                        response.request().httpMethod(),   // 3) HTTP method
                        null,                              // 4) cause
                        (Date) null,                       // 5) retryAfter
                        response.request());               // 6) original request
            }
            return new ErrorDecoder.Default().decode(methodKey, response);
        };
    }
}


