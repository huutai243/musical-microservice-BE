package vn.com.iuh.fit.user_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.iuh.fit.user_service.dto.UpdateProfileRequest;
import vn.com.iuh.fit.user_service.dto.UserRequest;
import vn.com.iuh.fit.user_service.entity.User;
import vn.com.iuh.fit.user_service.service.UserService;
import vn.com.iuh.fit.user_service.service.MinioService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final MinioService minioService;

    // Lấy thông tin cá nhân từ JWT
    @GetMapping("/me")
    public ResponseEntity<User> getUserInfo() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); // 🔹 Lấy ID từ SecurityContext

        Optional<User> userEntity = userService.getUserById(userId);
        return userEntity.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    // Cập nhật avatar user
    @PutMapping(value = "/update-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> updateAvatar(@RequestPart(value = "image", required = false) MultipartFile imageFile) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Cập nhật avatar cho người dùng
        User updatedUser = userService.updateAvatar(userId, imageFile);
        return ResponseEntity.ok(updatedUser);
    }

    // Cập nhật địa chỉ, số điện thoại
    @PutMapping("/update-profile")
    public ResponseEntity<User> updateProfile(@RequestBody UpdateProfileRequest updateProfileRequest) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Cập nhật thông tin người dùng
        User updatedUser = userService.updateProfile(userId, updateProfileRequest);
        return ResponseEntity.ok(updatedUser);
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
