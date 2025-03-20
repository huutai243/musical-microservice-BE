package vn.com.iuh.fit.order_service.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.order_service.dto.CheckoutEventDTO;
import vn.com.iuh.fit.order_service.event.InventoryValidationResultEvent;
import vn.com.iuh.fit.order_service.service.OrderService;

@Service
public class OrderConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConsumer.class);

    private final OrderService orderService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderConsumer(OrderService orderService,
                         KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderService = orderService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
            topics = "checkout-events",
            groupId = "order-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void processCheckoutEvent(CheckoutEventDTO checkoutEvent) {
        try {
            LOGGER.info(" Nhận CheckoutEvent từ Kafka: {}", checkoutEvent);
            orderService.createOrderFromCheckout(checkoutEvent);
        } catch (Exception e) {
            LOGGER.error(" Lỗi xử lý Checkout Event: ", e);
        }
    }


    @KafkaListener(
            topics = "inventory-validation-result",
            groupId = "order-group-new",
            containerFactory = "inventoryValidationResultListenerFactory"
            )
    public void processInventoryValidationResult(InventoryValidationResultEvent event) {
        try {
            LOGGER.info(" Nhận kết quả kiểm tra tồn kho từ Kafka: {}", event);
            orderService.handleInventoryValidationResult(event);
        } catch (Exception e) {
            LOGGER.error(" Lỗi khi xử lý kết quả kiểm tra tồn kho: ", e);
        }
    }

}

