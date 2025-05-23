package vn.com.iuh.fit.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl; // Đổi tên từ avatar thành avatarUrl
    private String phoneNumber;
    private String address;
}