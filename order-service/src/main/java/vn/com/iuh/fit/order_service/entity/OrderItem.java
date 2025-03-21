package vn.com.iuh.fit.order_service.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.com.iuh.fit.order_service.enums.OrderItemStatus;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private String productId;
    private String name;
    private Double price;
    private Integer quantity;
    private String imageUrl;

    @Enumerated(EnumType.STRING) // Trạng thái riêng cho từng sản phẩm
    private OrderItemStatus status;
}
