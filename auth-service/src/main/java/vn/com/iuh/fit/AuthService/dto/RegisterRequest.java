package vn.com.iuh.fit.AuthService.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private Long id;
    private String username;
    private String email;
    private String password;
}

