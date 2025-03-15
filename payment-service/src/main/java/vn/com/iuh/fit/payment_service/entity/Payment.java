package vn.com.iuh.fit.payment_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private String userId;
    private Double amount;
    private String paymentMethod; // STRIPE, VNPAY, MOMO, PAYPAL
    private String status; // PENDING, SUCCESS, FAILED
    private LocalDateTime createdAt;
}
