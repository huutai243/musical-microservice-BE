package vn.com.iuh.fit.user_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
    //Update avatar
    @PostMapping("/me/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 🔹 Kiểm tra file có rỗng không
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ File is empty. Please upload a valid file.");
            }

            // 🔹 Kiểm tra dung lượng file (ví dụ: tối đa 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ File size exceeds 5MB limit.");
            }

            // 🔹 Định dạng tên file để tránh trùng lặp
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg"; // Mặc định nếu không có extension

            String uniqueFileName = UUID.randomUUID() + fileExtension;

            // 🔹 Upload file lên MinIO
            String filePath = minioService.uploadFile(file, uniqueFileName);

            // 🔹 Lấy thông tin user từ JWT
            Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 🔹 Cập nhật avatar cho user
            user.setAvatar(filePath);
            userService.updateUser(userId, user);

            return ResponseEntity.ok("✅ Avatar uploaded successfully: " + filePath);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Failed to upload avatar due to unexpected error.");
        }
    }


}
