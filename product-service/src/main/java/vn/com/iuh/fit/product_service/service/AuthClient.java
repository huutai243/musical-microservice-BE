package vn.com.iuh.fit.product_service.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service")
public interface AuthClient {
    @GetMapping("/api/auth/validate")
    boolean validateToken(@RequestHeader("Authorization") String token);
}


