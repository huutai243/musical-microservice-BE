package vn.com.iuh.fit.AuthService.client;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import vn.com.iuh.fit.AuthService.config.FeignRetryConfig;
import vn.com.iuh.fit.AuthService.dto.UserRequest;

@FeignClient(
        name = "user-service",
        url = "http://api-gateway:9000",
        configuration = FeignRetryConfig.class
)
public interface UserServiceClient {
    @PostMapping("/api/users/create")
    void createUser(@RequestBody UserRequest userRequest);
}
