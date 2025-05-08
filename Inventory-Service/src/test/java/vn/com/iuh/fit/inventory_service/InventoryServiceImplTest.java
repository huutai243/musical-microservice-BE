package vn.com.iuh.fit.inventory_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import vn.com.iuh.fit.inventory_service.dto.InventoryValidationItem;
import vn.com.iuh.fit.inventory_service.entity.Inventory;
import vn.com.iuh.fit.inventory_service.entity.InventoryReservation;
import vn.com.iuh.fit.inventory_service.entity.OutboxEvent;
import vn.com.iuh.fit.inventory_service.event.InventoryDeductionRequestEvent;
import vn.com.iuh.fit.inventory_service.producer.InventoryProducer;
import vn.com.iuh.fit.inventory_service.repository.InventoryRepository;
import vn.com.iuh.fit.inventory_service.repository.InventoryReservationRepository;
import vn.com.iuh.fit.inventory_service.repository.OutboxEventRepository;
import vn.com.iuh.fit.inventory_service.service.impl.InventoryServiceImpl;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryReservationRepository reservationRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private InventoryProducer inventoryProducer;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        inventoryService = new InventoryServiceImpl(
                inventoryRepository,
                inventoryProducer,
                objectMapper,
                outboxEventRepository,
                reservationRepository
        );
    }

    // === getStock() ===
    @Test
    void testGetStock_NoInventory_ShouldReturnZero() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());
        int stock = inventoryService.getStock(1L);
        assertEquals(0, stock);
    }

    @Test
    void testGetStock_WithReservations_ShouldReturnAvailable() {
        Inventory inventory = Inventory.builder().productId(1L).quantity(100).build();
        List<InventoryReservation> reservations = List.of(
                InventoryReservation.builder().reservedQuantity(30).expireAt(LocalDateTime.now().plusMinutes(5)).build()
        );
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(reservationRepository.findByProductIdAndStatus(1L, "ACTIVE")).thenReturn(reservations);
        int stock = inventoryService.getStock(1L);
        assertEquals(70, stock);
    }

    // === updateStock() ===
    @Test
    void testUpdateStock_QuantityLessThanReserved_ShouldThrow() {
        Inventory inventory = Inventory.builder().productId(1L).quantity(50).build();
        List<InventoryReservation> reservations = List.of(
                InventoryReservation.builder().reservedQuantity(15).expireAt(LocalDateTime.now().plusMinutes(5)).build()
        );
        when(inventoryRepository.findByProductIdForUpdate(1L)).thenReturn(Optional.of(inventory));
        when(reservationRepository.findByProductIdAndStatus(1L, "ACTIVE")).thenReturn(reservations);
        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.updateStock(1L, 10)
        );
    }

    @Test
    void testUpdateStock_SameQuantity_ShouldSkipSave() {
        Inventory inventory = Inventory.builder().productId(1L).quantity(50).build();
        when(inventoryRepository.findByProductIdForUpdate(1L)).thenReturn(Optional.of(inventory));
        when(reservationRepository.findByProductIdAndStatus(1L, "ACTIVE")).thenReturn(Collections.emptyList());
        inventoryService.updateStock(1L, 50);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void testUpdateStock_NewQuantity_ShouldSave() {
        Inventory inventory = Inventory.builder().productId(1L).quantity(50).build();
        when(inventoryRepository.findByProductIdForUpdate(1L)).thenReturn(Optional.of(inventory));
        when(reservationRepository.findByProductIdAndStatus(1L, "ACTIVE")).thenReturn(Collections.emptyList());
        inventoryService.updateStock(1L, 80);
        verify(inventoryRepository).save(any());
    }

    // === validateInventory() ===
    @Test
    void testValidateInventory_AllConfirmed_ShouldCreateReservation() {
        InventoryValidationItem item = new InventoryValidationItem(1L, 5);
        Inventory inventory = Inventory.builder().productId(1L).quantity(10).build();
        when(inventoryRepository.findByProductIdForUpdate(1L)).thenReturn(Optional.of(inventory));
        when(reservationRepository.findByProductIdAndStatus(1L, "ACTIVE")).thenReturn(Collections.emptyList());
        inventoryService.validateInventory(100L, List.of(item));
        verify(reservationRepository).save(any());
        verify(outboxEventRepository).save(any());
    }

    @Test
    void testValidateInventory_OutOfStock_ShouldSkipReservation() {
        InventoryValidationItem item = new InventoryValidationItem(1L, 20);
        Inventory inventory = Inventory.builder().productId(1L).quantity(10).build();
        when(inventoryRepository.findByProductIdForUpdate(1L)).thenReturn(Optional.of(inventory));
        when(reservationRepository.findByProductIdAndStatus(1L, "ACTIVE")).thenReturn(Collections.emptyList());
        inventoryService.validateInventory(100L, List.of(item));
        verify(reservationRepository, never()).save(any());
        verify(outboxEventRepository).save(any());
    }

    // === deductStock() ===
    @Test
    void testDeductStock_EnoughReserved_ShouldDeduct() {
        Long orderId = 100L;
        Long productId = 1L;
        Inventory inventory = Inventory.builder().productId(productId).quantity(50).build();
        List<InventoryReservation> reservations = List.of(
                InventoryReservation.builder().orderId(orderId).productId(productId).reservedQuantity(5).status("ACTIVE").build()
        );
        InventoryDeductionRequestEvent.ProductQuantity pq = new InventoryDeductionRequestEvent.ProductQuantity(productId.toString(), 5);
        when(inventoryRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(inventory));
        when(reservationRepository.findByOrderIdAndProductIdAndStatus(orderId, productId, "ACTIVE")).thenReturn(reservations);
        inventoryService.deductStock(orderId, List.of(pq));
        verify(inventoryRepository).save(any());
        verify(reservationRepository, atLeastOnce()).save(any());
    }

    @Test
    void testDeductStock_NotEnoughReserved_ShouldThrow() {
        Long orderId = 100L;
        Long productId = 1L;
        Inventory inventory = Inventory.builder().productId(productId).quantity(50).build();
        List<InventoryReservation> reservations = List.of(
                InventoryReservation.builder().orderId(orderId).productId(productId).reservedQuantity(2).status("ACTIVE").build()
        );
        InventoryDeductionRequestEvent.ProductQuantity pq = new InventoryDeductionRequestEvent.ProductQuantity(productId.toString(), 5);
        when(inventoryRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(inventory));
        when(reservationRepository.findByOrderIdAndProductIdAndStatus(orderId, productId, "ACTIVE")).thenReturn(reservations);
        assertThrows(RuntimeException.class, () ->
                inventoryService.deductStock(orderId, List.of(pq))
        );
    }

    @Test
    void testDeductStock_ReservationGreaterThanNeed_ShouldSplit() {
        Long orderId = 100L;
        Long productId = 1L;

        Inventory inventory = Inventory.builder().productId(productId).quantity(100).build();
        List<InventoryReservation> reservations = List.of(
                InventoryReservation.builder().orderId(orderId).productId(productId).reservedQuantity(10).status("ACTIVE").expireAt(LocalDateTime.now().plusMinutes(5)).build()
        );

        InventoryDeductionRequestEvent.ProductQuantity pq = new InventoryDeductionRequestEvent.ProductQuantity(productId.toString(), 5);

        when(inventoryRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(inventory));
        when(reservationRepository.findByOrderIdAndProductIdAndStatus(orderId, productId, "ACTIVE")).thenReturn(reservations);

        inventoryService.deductStock(orderId, List.of(pq));

        // Reservation ban đầu còn lại 5, phần mới tạo USED là 5
        verify(reservationRepository, times(2)).save(any());
    }

    @Test
    void testDeductStock_InventoryNotFound_ShouldThrow() {
        Long orderId = 100L;
        Long productId = 1L;

        InventoryDeductionRequestEvent.ProductQuantity pq = new InventoryDeductionRequestEvent.ProductQuantity(productId.toString(), 5);
        when(inventoryRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                inventoryService.deductStock(orderId, List.of(pq))
        );
    }

    @Test
    void testDeductStock_NoReservation_ShouldThrow() {
        Long orderId = 100L;
        Long productId = 1L;

        Inventory inventory = Inventory.builder().productId(productId).quantity(50).build();
        InventoryDeductionRequestEvent.ProductQuantity pq = new InventoryDeductionRequestEvent.ProductQuantity(productId.toString(), 5);

        when(inventoryRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(inventory));
        when(reservationRepository.findByOrderIdAndProductIdAndStatus(orderId, productId, "ACTIVE")).thenReturn(Collections.emptyList());

        assertThrows(RuntimeException.class, () ->
                inventoryService.deductStock(orderId, List.of(pq))
        );
    }

    @Test
    void testValidateInventory_PartialConfirmed_ShouldSaveOne() {
        InventoryValidationItem item1 = new InventoryValidationItem(1L, 5); // đủ
        InventoryValidationItem item2 = new InventoryValidationItem(2L, 15); // thiếu

        Inventory inv1 = Inventory.builder().productId(1L).quantity(10).build();
        Inventory inv2 = Inventory.builder().productId(2L).quantity(10).build();

        when(inventoryRepository.findByProductIdForUpdate(1L)).thenReturn(Optional.of(inv1));
        when(inventoryRepository.findByProductIdForUpdate(2L)).thenReturn(Optional.of(inv2));
        when(reservationRepository.findByProductIdAndStatus(anyLong(), anyString())).thenReturn(Collections.emptyList());

        inventoryService.validateInventory(123L, List.of(item1, item2));

        verify(reservationRepository, times(1)).save(any()); // chỉ có item1 được giữ
        verify(outboxEventRepository).save(any()); // vẫn gửi outbox
    }

    @Test
    void testValidateInventory_JsonSerializeFail_ShouldThrowRuntime() throws Exception {
        InventoryValidationItem item = new InventoryValidationItem(1L, 5);
        Inventory inv = Inventory.builder().productId(1L).quantity(10).build();

        ObjectMapper mockFailingMapper = mock(ObjectMapper.class);
        when(inventoryRepository.findByProductIdForUpdate(1L)).thenReturn(Optional.of(inv));
        when(reservationRepository.findByProductIdAndStatus(1L, "ACTIVE")).thenReturn(Collections.emptyList());
        when(mockFailingMapper.writeValueAsString(any())).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("Simulated error") {
        });

        InventoryServiceImpl faultyService = new InventoryServiceImpl(
                inventoryRepository, inventoryProducer, mockFailingMapper, outboxEventRepository, reservationRepository
        );

        assertThrows(RuntimeException.class, () ->
                faultyService.validateInventory(200L, List.of(item))
        );
    }
}
