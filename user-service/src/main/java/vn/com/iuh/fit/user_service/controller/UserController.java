package vn.com.iuh.fit.user_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.com.iuh.fit.user_service.dto.UserRequest;
import vn.com.iuh.fit.user_service.entity.User;
import vn.com.iuh.fit.user_service.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     *  Lấy thông tin người dùng hiện tại từ JWT
     */
    @GetMapping("/me")
    public ResponseEntity<User> getUserInfo() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userEntity = userService.getUserById(userId);
        return userEntity.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     *  Người dùng cập nhật chính họ
     */
    @PutMapping("/me")
    public ResponseEntity<User> updateUser(@RequestBody User userRequest) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.updateUser(userId, userRequest));
    }

    /**
     *  ADMIN tạo user mới
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody UserRequest userRequest) {
        if (userService.getUserById(userRequest.getId()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        User newUser = userService.createUser(userRequest);
        return ResponseEntity.ok(newUser);
    }

    /**
     *  ADMIN xem tất cả user
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     *  ADMIN lấy chi tiết user theo ID
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     *  ADMIN cập nhật user bất kỳ theo ID
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUserByAdmin(@PathVariable Long id, @RequestBody User userRequest) {
        return ResponseEntity.ok(userService.updateUser(id, userRequest));
    }

    /**
     * ADMIN xoá user theo ID
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
