package vn.com.iuh.fit.AuthService.service;

import vn.com.iuh.fit.AuthService.dto.*;
import vn.com.iuh.fit.AuthService.entity.User;
import java.util.HashMap;
import java.util.Map;

public interface AuthService {
    JwtResponse login(LoginRequest request);
    UserDto register(RegisterRequest request);
    void forgotPassword(String email);
    JwtResponse refreshToken(String refreshToken);
    Map<String, String> verifyEmail(String token);
    void logout(String refreshToken);
}
