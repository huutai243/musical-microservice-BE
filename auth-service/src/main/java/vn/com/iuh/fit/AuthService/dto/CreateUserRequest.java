package vn.com.iuh.fit.AuthService.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    private String username;
    private String email;
    private String password;
    private String role; // "ADMIN" hoặc "USER"
}

