package vn.com.iuh.fit.AuthService.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.iuh.fit.AuthService.dto.*;
import vn.com.iuh.fit.AuthService.entity.Role;
import vn.com.iuh.fit.AuthService.entity.RoleName;
import vn.com.iuh.fit.AuthService.entity.User;
import vn.com.iuh.fit.AuthService.repository.RoleRepository;
import vn.com.iuh.fit.AuthService.repository.UserRepository;
import vn.com.iuh.fit.AuthService.service.AuthService;
import vn.com.iuh.fit.AuthService.config.JwtService;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Đăng ký tài khoản mới
     */
    @RateLimiter(name = "authRateLimiter", fallbackMethod = "handleRateLimit")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.");
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> handleRateLimit(RegisterRequest request, Throwable ex) {
        return ResponseEntity
                .status(429)
                .body(Map.of(
                        "error", "Too many register attempts. Please try again later.",
                        "message", "Bạn đã đăng ký quá nhiều lần. Vui lòng thử lại sau 1 phút!"
                ));
    }

    /**
     * Đăng nhập và nhận JWT
     */
//    @RateLimiter(name = "authRateLimiter", fallbackMethod = "handleRateLimit")
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    public ResponseEntity<?> handleRateLimit(LoginRequest request, Exception ex) {
        return ResponseEntity
                .status(429)
                .body(Map.of(
                        "error", "Too many login attempts. Please try again later.",
                        "message", "Bạn đã đăng nhập quá nhiều lần. Vui lòng thử lại sau 1 phút!"
                ));
    }

    /**
     * Đăng xuất và xóa refresh token
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok("Đăng xuất thành công.");
    }


    /**
     * Refresh Access Token từ Refresh Token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<JwtResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(jwtService.refreshToken(request.getRefreshToken()));
    }

    /**
     * Quên mật khẩu - Gửi email để đặt lại mật khẩu
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok("Email đặt lại mật khẩu đã được gửi.");
    }

    /**
     * Xác thực email từ đường link được gửi
     */
    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String token) {
        boolean isVerified = authService.verifyEmail(token);

        Map<String, Object> response = new HashMap<>();
        if (isVerified) {
            response.put("message", "Xác thực email thành công!");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Token không hợp lệ hoặc đã hết hạn.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    @GetMapping("/user/{id}/email")
    public ResponseEntity<String> getEmailByUserId(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
        return ResponseEntity.ok(user.getEmail());
    }

    /**
     * Test giải mã JWT để lấy claims
     */
    @GetMapping("/decode-token")
    public ResponseEntity<Map<String, Object>> decodeToken(@RequestHeader("Authorization") String token) {
        // Loại bỏ "Bearer " khỏi token nếu có
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Giải mã JWT để lấy toàn bộ claims
        Claims claims = jwtService.extractAllClaims(token);

        return ResponseEntity.ok(claims);
    }

    /**
     * Admin thay đổi role người dùng
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/change-role")
    public ResponseEntity<?> updateUserRole(@RequestBody UpdateUserRoleRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + request.getUserId()));

        String roleInput = request.getNewRole().toUpperCase();
        if (!roleInput.equals("ADMIN") && !roleInput.equals("USER")) {
            return ResponseEntity.badRequest().body("Role không hợp lệ. Chỉ chấp nhận: ADMIN hoặc USER");
        }

        Role newRoleEntity = roleRepository.findByName(roleInput)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role: " + roleInput));

        Collection<Role> updatedRoles = new ArrayList<>();
        updatedRoles.add(newRoleEntity);
        user.setRoles(updatedRoles);

        userRepository.save(user);

        return ResponseEntity.ok("Cập nhật role thành công.");
    }

    /**
     * Lấy danh sách role của người dùng theo ID
     */
    @GetMapping("/user/{id}/role")
    public ResponseEntity<?> getUserRoles(@PathVariable Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng với ID: " + id);
        }

        User user = optionalUser.get();
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)  // getName() là String
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("userId", id);
        response.put("roles", roleNames);

        return ResponseEntity.ok(response);
    }
    /**
     * Admin tạo tài khoản mới (không cần xác nhận email)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-user")
    public ResponseEntity<Map<String, Object>> createUserByAdmin(@RequestBody CreateUserByAdminRequest request) {
        UserDto userDto = authService.createUserByAdmin(request);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tài khoản đã được tạo thành công.");
        response.put("user", userDto);
        return ResponseEntity.ok(response);
    }

//    @PreAuthorize("hasRole('ADMIN')")
//    @PostMapping("/admin/create-user")
//    public ResponseEntity<?> createUserByAdmin(@RequestBody CreateUserRequest request) {
//        try {
//            UserDto createdUser = authService.createUserByAdmin(request);
//            return ResponseEntity.ok(createdUser);
//        } catch (IllegalArgumentException ex) {
//            return ResponseEntity.badRequest().body(ex.getMessage());
//        } catch (Exception ex) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + ex.getMessage());
//        }
//    }
}
