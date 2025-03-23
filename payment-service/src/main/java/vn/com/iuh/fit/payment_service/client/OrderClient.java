package vn.com.iuh.fit.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import vn.com.iuh.fit.payment_service.dto.OrderResponseDTO;

@FeignClient(name = "order-service", path = "/api/orders/internal")
public interface OrderClient {

    @GetMapping("/{orderId}")
    OrderResponseDTO getOrderById(@PathVariable("orderId") Long orderId);
}
