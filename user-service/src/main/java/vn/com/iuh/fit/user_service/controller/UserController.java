package vn.com.iuh.fit.user_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.com.iuh.fit.user_service.dto.UserRequest;
import vn.com.iuh.fit.user_service.entity.User;
import vn.com.iuh.fit.user_service.service.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // Lấy thông tin cá nhân từ JWT
    @GetMapping("/me")
    public ResponseEntity<User> getUserInfo() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); // 🔹 Lấy ID từ SecurityContext

        Optional<User> userEntity = userService.getUserById(userId);
        return userEntity.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    // Cập nhật thông tin theo id
    @PutMapping("/me")
    public ResponseEntity<User> updateUser(@RequestBody User userRequest) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.updateUser(userId, userRequest));
    }
    //Create
    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody UserRequest userRequest) {
        if (userService.getUserById(userRequest.getId()).isPresent()) {
            return ResponseEntity.badRequest().build(); // Nếu user đã tồn tại, trả về lỗi
        }
        User newUser = userService.createUser(userRequest);
        return ResponseEntity.ok(newUser);
    }

}
