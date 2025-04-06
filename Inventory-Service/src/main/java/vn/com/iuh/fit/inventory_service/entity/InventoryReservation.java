package vn.com.iuh.fit.inventory_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID sản phẩm đang được giữ
    @Column(nullable = false)
    private Long productId;

    // ID đơn hàng giữ hàng (liên kết orderId nếu cần)
    @Column(nullable = false)
    private Long orderId;

    // Số lượng đang giữ
    @Column(nullable = false)
    private Integer reservedQuantity;

    // Thời gian hết hạn giữ hàng (để auto release)
    @Column(nullable = false)
    private LocalDateTime expireAt;

    // Trạng thái giữ hàng
    @Column(nullable = false)
    private String status; // e.g. "ACTIVE", "EXPIRED", "USED"
}
