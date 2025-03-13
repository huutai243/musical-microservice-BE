package vn.com.iuh.fit.order_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String productId;
    private Integer quantity;
    private Double price;

    private String status; // "PENDING", "CONFIRMED", "CANCELLED"

    private LocalDateTime createdAt;
}
