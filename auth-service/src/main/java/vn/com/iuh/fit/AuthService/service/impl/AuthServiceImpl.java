package vn.com.iuh.fit.AuthService.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.AuthService.config.JwtService;
import vn.com.iuh.fit.AuthService.dto.*;
import vn.com.iuh.fit.AuthService.entity.*;
import vn.com.iuh.fit.AuthService.repository.*;
import vn.com.iuh.fit.AuthService.service.AuthService;
import vn.com.iuh.fit.AuthService.service.EmailService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final SecureTokenRepository secureTokenRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * 🛠 **Xử lý đăng nhập**
     */
    @Override
    public JwtResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            throw new RuntimeException("Sai tên đăng nhập hoặc mật khẩu.");
        }

        // Xác thực user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userOpt.get();
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Lưu Refresh Token vào DB
        RefreshToken savedToken = new RefreshToken();
        savedToken.setToken(refreshToken);
        savedToken.setUser(user);
        savedToken.setExpiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60));
        refreshTokenRepository.save(savedToken);

        return new JwtResponse(accessToken, refreshToken);
    }

    /**
     * Đăng xuất - Xóa Refresh Token
     */
    @Override
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }


    /**
     * 🛠 **Xử lý đăng ký User mới**
     */
    @Override
    public UserDto register(RegisterRequest request) {
        // Kiểm tra xem tên đăng nhập đã tồn tại chưa
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }

        // Kiểm tra xem email đã tồn tại chưa
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        // Lấy vai trò mặc định "ROLE_USER"
        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Vai trò mặc định không tồn tại"));

        // Tạo User mới và gán vai trò USER
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false)
                .provider("local")
                .roles(Collections.singletonList(defaultRole))  // Gán ROLE_USER cho tài khoản mới
                .build();

        userRepository.save(user);

        // Gửi email xác thực
        sendVerificationEmail(user);

        return new UserDto(user.getUsername(), user.getEmail(), false);
    }


    /**
     * 🛠 **Quên mật khẩu**
     */
    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống."));

        // Tạo token đặt lại mật khẩu
        SecureToken token = new SecureToken();
        String tokenValue = UUID.randomUUID().toString();
        token.setToken(tokenValue);
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(1)); // Token hết hạn sau 1 giờ
        secureTokenRepository.save(token);

        // Gửi email đặt lại mật khẩu
        String resetLink = "http://localhost:3000/reset-password?token=" + tokenValue;
        String emailBody = "Nhấn vào link để đặt lại mật khẩu: <a href=\"" + resetLink + "\">Click here</a>";
        emailService.sendEmail(user.getEmail(), "Đặt lại mật khẩu", emailBody);
    }

    /**
     * 🛠 **Làm mới Access Token bằng Refresh Token**
     */
    @Override
    public JwtResponse refreshToken(String refreshToken) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken);
        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) {
            throw new RuntimeException("Refresh token không hợp lệ hoặc đã hết hạn.");
        }

        User user = tokenOpt.get().getUser();
        String newAccessToken = jwtService.generateAccessToken(user);

        return new JwtResponse(newAccessToken, refreshToken);
    }

    /**
     * 🛠 **Gửi email xác thực tài khoản**
     */
    public void sendVerificationEmail(User user) {
        SecureToken token = new SecureToken();
        String tokenValue = UUID.randomUUID().toString();
        token.setToken(tokenValue);
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(24));
        secureTokenRepository.save(token);
        String verificationLink = "http://localhost:3000/verify-email?token=" + tokenValue;
        String emailBody = "Vui lòng nhấn vào link sau để xác thực email: <a href=\"" + verificationLink + "\">Click here</a>";
        emailService.sendEmail(user.getEmail(), "Xác thực tài khoản", emailBody);
    }

    /**
     * 🛠 **Xác thực tài khoản qua email**
     */
    @Override
    public Boolean verifyEmail(String token) {
        SecureToken tokenEntity = secureTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ."));

        if (tokenEntity.isExpired()) {
            return false; // Token hết hạn
        }
        User user = tokenEntity.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        return true;
    }

}
