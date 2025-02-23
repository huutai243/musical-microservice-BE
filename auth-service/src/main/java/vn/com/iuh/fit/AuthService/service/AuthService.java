package vn.com.iuh.fit.AuthService.service;

import vn.com.iuh.fit.AuthService.dto.*;
import vn.com.iuh.fit.AuthService.entity.User;

public interface AuthService {
    JwtResponse login(LoginRequest request);
    UserDto register(RegisterRequest request);
    void forgotPassword(String email);
    JwtResponse refreshToken(String refreshToken);
    String verifyEmail(String token);
}
