package vn.com.iuh.fit.AuthService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.iuh.fit.AuthService.dto.*;
import vn.com.iuh.fit.AuthService.service.AuthService;
import vn.com.iuh.fit.AuthService.config.JwtService;

import java.util.HashMap;
import java.util.Map;
import org.springframework.ui.Model;

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
    public String verifyEmail(@RequestParam String token, Model model) {
        try {
            Map<String, String> response = authService.verifyEmail(token);
            model.addAttribute("message", response.get("message")); // Truyền thông báo vào model
            model.addAttribute("redirectUrl", response.get("redirectUrl")); // Truyền URL chuyển hướng vào model
            return "email-verified"; // Trả về tên template HTML
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage()); // Truyền thông báo lỗi vào model
            return "error-page"; // Trả về tên template HTML cho lỗi
        }
    }
}
