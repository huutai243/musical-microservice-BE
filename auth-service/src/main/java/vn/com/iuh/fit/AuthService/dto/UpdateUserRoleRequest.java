package vn.com.iuh.fit.AuthService.dto;

import lombok.Data;

@Data
public class UpdateUserRoleRequest {
    private Long userId;
    private String newRole;
}
