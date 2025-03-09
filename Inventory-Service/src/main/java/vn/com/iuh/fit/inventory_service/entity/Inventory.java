package vn.com.iuh.fit.inventory_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private int quantity;

    public Inventory(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}
