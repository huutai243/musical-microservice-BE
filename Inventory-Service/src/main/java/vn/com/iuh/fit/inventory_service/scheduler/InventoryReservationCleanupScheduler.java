package vn.com.iuh.fit.inventory_service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.com.iuh.fit.inventory_service.entity.InventoryReservation;
import vn.com.iuh.fit.inventory_service.repository.InventoryReservationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryReservationCleanupScheduler {

    private final InventoryReservationRepository reservationRepository;

    /**
     * Cron chạy mỗi 1 phút để huỷ giữ hàng đã hết hạn
     */
    @Scheduled(fixedDelay = 60000) // mỗi 60s
    public void cleanUpExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();

        List<InventoryReservation> expiredReservations = reservationRepository
                .findByExpireAtBeforeAndStatus(now, "ACTIVE");

        if (expiredReservations.isEmpty()) {
            log.debug("[ExpiredCleanup] Không có giữ hàng nào hết hạn.");
            return;
        }

        log.info("[ExpiredCleanup] Bắt đầu xử lý {} giữ hàng đã hết hạn", expiredReservations.size());

        for (InventoryReservation reservation : expiredReservations) {
            try {
                reservation.setStatus("CANCELLED");
                reservationRepository.save(reservation);

                log.info("[Huỷ giữ hàng] Order #{} | Product #{} | Qty={} đã bị huỷ do hết hạn",
                        reservation.getOrderId(),
                        reservation.getProductId(),
                        reservation.getReservedQuantity());

            } catch (Exception ex) {
                log.error("[ExpiredCleanup] Lỗi khi huỷ giữ hàng ID={} | Error: {}",
                        reservation.getId(), ex.getMessage(), ex);
            }
        }
    }
}
