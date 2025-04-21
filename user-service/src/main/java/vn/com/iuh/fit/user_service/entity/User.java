package vn.com.iuh.fit.user_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private Long id; // Lấy từ Auth-Service

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNumber;
    private String address;

    private String avatarUrl;
}
