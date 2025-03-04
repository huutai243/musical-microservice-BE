package vn.com.iuh.fit.AuthService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import vn.com.iuh.fit.AuthService.dto.UserRequest;

@FeignClient(name = "user-service", url = "http://api-gateway:9000")
public interface UserServiceClient {

    @PostMapping("/api/users/create")
    void createUser(@RequestBody UserRequest userRequest);
}
