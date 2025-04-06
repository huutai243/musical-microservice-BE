package vn.com.iuh.fit.inventory_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.iuh.fit.inventory_service.entity.InventoryReservation;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

    List<InventoryReservation> findByOrderIdAndStatus(Long orderId, String status);

    List<InventoryReservation> findByOrderIdAndProductIdAndStatus(Long orderId, Long productId, String status);

    List<InventoryReservation> findByExpireAtBeforeAndStatus(LocalDateTime time, String status);

    List<InventoryReservation> findByProductIdAndStatus(Long productId, String status);
}
