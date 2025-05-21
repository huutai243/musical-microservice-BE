package vn.com.iuh.fit.AuthService.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserByAdminRequest {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String role;
}