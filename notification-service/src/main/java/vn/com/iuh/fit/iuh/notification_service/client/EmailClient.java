package vn.com.iuh.fit.iuh.notification_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface EmailClient {
    @GetMapping("/api/auth/user/{id}/email")
    String getEmailByUserId(@PathVariable("id") Long id);
}

