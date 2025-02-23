package vn.com.iuh.fit.AuthService.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private boolean emailVerified;

    public UserDto(String username, String email, boolean emailVerified) {
        this.username = username;
        this.email = email;
        this.emailVerified = emailVerified;
    }
}

