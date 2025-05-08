package vn.com.iuh.fit.AuthService.service;

import vn.com.iuh.fit.AuthService.dto.*;

public interface AuthService {
    JwtResponse login(LoginRequest request);
    UserDto register(RegisterRequest request);
    void forgotPassword(String email);
    JwtResponse refreshToken(String refreshToken);
    Boolean verifyEmail(String token);
    void logout(String refreshToken);
    UserDto createUserByAdmin(CreateUserRequest request);

}
