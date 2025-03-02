package vn.com.iuh.fit.AuthService.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.iuh.fit.AuthService.dto.*;
import vn.com.iuh.fit.AuthService.service.AuthService;
import vn.com.iuh.fit.AuthService.config.JwtService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    /**
     * Đăng ký tài khoản mới
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.");
        return ResponseEntity.ok(response);
    }

    /**
     * Đăng nhập và nhận JWT
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
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
}
