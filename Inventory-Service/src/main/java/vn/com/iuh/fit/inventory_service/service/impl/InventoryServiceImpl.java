package vn.com.iuh.fit.inventory_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.com.iuh.fit.inventory_service.dto.InventoryValidationItem;
import vn.com.iuh.fit.inventory_service.entity.Inventory;
import vn.com.iuh.fit.inventory_service.entity.InventoryReservation;
import vn.com.iuh.fit.inventory_service.entity.OutboxEvent;
import vn.com.iuh.fit.inventory_service.event.InventoryDeductionRequestEvent;
import vn.com.iuh.fit.inventory_service.event.InventoryValidationResultEvent;
import vn.com.iuh.fit.inventory_service.producer.InventoryProducer;
import vn.com.iuh.fit.inventory_service.repository.InventoryRepository;
import vn.com.iuh.fit.inventory_service.repository.InventoryReservationRepository;
import vn.com.iuh.fit.inventory_service.repository.OutboxEventRepository;
import vn.com.iuh.fit.inventory_service.service.InventoryService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryProducer inventoryProducer;
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceImpl.class);
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;
    private final InventoryReservationRepository inventoryReservationRepository;


    public InventoryServiceImpl(
            InventoryRepository inventoryRepository,
            InventoryProducer inventoryProducer,
            ObjectMapper objectMapper,
            OutboxEventRepository outboxEventRepository,
            InventoryReservationRepository inventoryReservationRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryProducer = inventoryProducer;
        this.objectMapper = objectMapper;
        this.outboxEventRepository = outboxEventRepository;
        this.inventoryReservationRepository = inventoryReservationRepository;
    }

    @Override
    public int getStock(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(null);

        if (inventory == null) {
            log.warn("Không tìm thấy sản phẩm trong tồn kho: productId={}", productId);
            return 0;
        }

        int totalQuantity = inventory.getQuantity();
        int reserved = 0;
        int available;

        try {
            LocalDateTime now = LocalDateTime.now();
            List<InventoryReservation> reservations =
                    inventoryReservationRepository.findByProductIdAndStatus(productId, "ACTIVE");

            reserved = reservations.stream()
                    .filter(r -> r.getExpireAt().isAfter(now))
                    .mapToInt(InventoryReservation::getReservedQuantity)
                    .sum();

            available = totalQuantity - reserved;
            available = Math.max(available, 0);
        } catch (Exception e) {
            log.error("Lỗi khi tính số lượng giữ hàng cho productId={}", productId, e);
            return totalQuantity;
        }

        log.info("Tồn kho sản phẩm [{}]: tổng={}, đang giữ={}, khả dụng={}", productId, totalQuantity, reserved, available);
        return available;
    }

    @Override
    @Transactional
    public void updateStock(Long productId, int newQuantity) {
        LocalDateTime now = LocalDateTime.now();

        Inventory inventory = inventoryRepository.findByProductIdForUpdate(productId)
                .orElseGet(() -> {
                    log.info("Không tìm thấy tồn kho cho sản phẩm [{}]. Tạo mới với số lượng 0.", productId);
                    return Inventory.builder()
                            .productId(productId)
                            .quantity(0)
                            .build();
                });

        List<InventoryReservation> reservations =
                inventoryReservationRepository.findByProductIdAndStatus(productId, "ACTIVE");

        int reserved = reservations.stream()
                .filter(r -> r.getExpireAt().isAfter(now))
                .mapToInt(InventoryReservation::getReservedQuantity)
                .sum();

        if (newQuantity < reserved) {
            log.warn("Từ chối cập nhật tồn kho cho sản phẩm [{}]: newQuantity={}, reserved={}", productId, newQuantity, reserved);
            throw new IllegalArgumentException(String.format(
                    "Không thể cập nhật tồn kho = %d vì đang có %d sản phẩm được giữ.", newQuantity, reserved
            ));
        }

        if (inventory.getQuantity() == newQuantity) {
            log.info("Tồn kho sản phẩm [{}] không thay đổi ({}). Bỏ qua cập nhật.", productId, newQuantity);
            return;
        }

        inventory.setQuantity(newQuantity);
        inventoryRepository.save(inventory);

        log.info("Cập nhật tồn kho sản phẩm [{}]: số lượng mới = {}, đang giữ = {}", productId, newQuantity, reserved);
    }

    /**
     * Kiểm tra tồn kho và gửi phản hồi đến `order-service`
     */
    @Override
    @Transactional
    public void validateInventory(Long orderId, List<InventoryValidationItem> items) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireAt = now.plusMinutes(10);
        List<InventoryValidationResultEvent.Item> validatedItems = new ArrayList<>();

        for (InventoryValidationItem item : items) {
            Long productId = item.getProductId();
            int requestedQty = item.getQuantity();

            // Lock tồn kho để tránh race condition
            Inventory inventory = inventoryRepository.findByProductIdForUpdate(productId).orElse(null);

            if (inventory == null) {
                validatedItems.add(new InventoryValidationResultEvent.Item(
                        productId.toString(), requestedQty, 0, "OUT_OF_STOCK"
                ));
                log.warn("Sản phẩm [{}] không tồn tại trong kho. OUT_OF_STOCK", productId);
                continue;
            }

            // Sau khi lock mới tính reservedQty
            int reservedQty = inventoryReservationRepository
                    .findByProductIdAndStatus(productId, "ACTIVE").stream()
                    .filter(r -> r.getExpireAt().isAfter(now))
                    .mapToInt(InventoryReservation::getReservedQuantity)
                    .sum();

            int availableQty = inventory.getQuantity() - reservedQty;

            if (availableQty >= requestedQty) {
                InventoryReservation reservation = InventoryReservation.builder()
                        .orderId(orderId)
                        .productId(productId)
                        .reservedQuantity(requestedQty)
                        .expireAt(expireAt)
                        .status("ACTIVE")
                        .build();

                inventoryReservationRepository.save(reservation);

                validatedItems.add(new InventoryValidationResultEvent.Item(
                        productId.toString(), requestedQty, availableQty, "CONFIRMED"
                ));

                log.info(" Đặt giữ thành công: Sản phẩm [{}], Giữ: {}, Khả dụng: {}", productId, requestedQty, availableQty);
            } else {
                validatedItems.add(new InventoryValidationResultEvent.Item(
                        productId.toString(), requestedQty, availableQty, "OUT_OF_STOCK"
                ));

                log.warn(" Không đủ tồn kho: Sản phẩm [{}], Cần: {}, Khả dụng: {}", productId, requestedQty, availableQty);
            }
        }

        // Xác định trạng thái tổng thể
        String status = determineStatus(validatedItems);
        String message = switch (status) {
            case "VALIDATED" -> "Tồn kho hợp lệ.";
            case "REJECTED" -> "Toàn bộ sản phẩm trong đơn hàng đã hết hàng.";
            default -> "Một số sản phẩm trong đơn hàng không đủ số lượng.";
        };

        // Gửi qua Outbox
        try {
            String payload = objectMapper.writeValueAsString(
                    new InventoryValidationResultEvent(orderId, status, message, validatedItems)
            );

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .id(UUID.randomUUID())
                    .aggregateType("Inventory")
                    .aggregateId(String.valueOf(orderId))
                    .type("InventoryValidationResultEvent")
                    .payload(payload)
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxEventRepository.save(outboxEvent);

            log.info(" Ghi OutboxEvent thành công cho orderId: {}, status: {}", orderId, status);
        } catch (JsonProcessingException e) {
            log.error(" Lỗi serialize InventoryValidationResultEvent", e);
            throw new RuntimeException("Không thể serialize sự kiện kiểm tra tồn kho", e);
        }
    }


    private String determineStatus(List<InventoryValidationResultEvent.Item> validatedItems) {
        boolean allOut = validatedItems.stream().allMatch(i -> "OUT_OF_STOCK".equals(i.getStatus()));
        boolean noneOut = validatedItems.stream().noneMatch(i -> "OUT_OF_STOCK".equals(i.getStatus()));

        if (noneOut) return "VALIDATED";
        if (allOut) return "REJECTED";
        return "PARTIALLY_VALIDATED";
    }


    @Override
    @Transactional
    public void deductStock(Long orderId, List<InventoryDeductionRequestEvent.ProductQuantity> products) {
        LOGGER.info(" Trừ tồn kho cho đơn hàng #{} với {} sản phẩm", orderId, products.size());

        for (InventoryDeductionRequestEvent.ProductQuantity item : products) {
            Long productId = Long.parseLong(item.getProductId());
            Integer quantityToDeduct = item.getQuantity();

            Inventory inventory = inventoryRepository.findByProductIdForUpdate(productId)
                    .orElseThrow(() -> new RuntimeException(" Sản phẩm không tồn tại: " + productId));

            // Lấy các bản ghi reservation còn hiệu lực
            List<InventoryReservation> reservations = inventoryReservationRepository
                    .findByOrderIdAndProductIdAndStatus(orderId, productId, "ACTIVE");

            int totalReserved = reservations.stream()
                    .mapToInt(InventoryReservation::getReservedQuantity)
                    .sum();

            if (totalReserved < quantityToDeduct) {
                throw new RuntimeException(String.format(
                        " Không đủ hàng đã giữ để trừ cho sản phẩm %s. Đã giữ: %d, cần trừ: %d",
                        productId, totalReserved, quantityToDeduct));
            }

            // Trừ vào tồn kho
            inventory.setQuantity(inventory.getQuantity() - quantityToDeduct);
            inventoryRepository.save(inventory);

            int remainingToDeduct = quantityToDeduct;

            // Duyệt và đánh dấu các reservation là USED
            for (InventoryReservation res : reservations) {
                if (remainingToDeduct <= 0) break;

                int resQty = res.getReservedQuantity();

                if (resQty <= remainingToDeduct) {
                    // Dùng hết reservation này
                    res.setStatus("USED");
                    remainingToDeduct -= resQty;
                } else {
                    // Dùng 1 phần → tách reservation (nếu cần)
                    res.setReservedQuantity(resQty - remainingToDeduct);

                    InventoryReservation usedPart = InventoryReservation.builder()
                            .orderId(orderId)
                            .productId(productId)
                            .reservedQuantity(remainingToDeduct)
                            .status("USED")
                            .expireAt(res.getExpireAt())
                            .build();

                    inventoryReservationRepository.save(usedPart);
                    remainingToDeduct = 0;
                }

                inventoryReservationRepository.save(res);
            }

            LOGGER.info(" Đã trừ {} sản phẩm [{}]. Tồn kho còn: {}", quantityToDeduct, productId, inventory.getQuantity());
        }

        LOGGER.info(" Hoàn tất trừ kho cho đơn hàng #{}", orderId);
    }
}

