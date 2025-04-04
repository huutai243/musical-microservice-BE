package vn.com.iuh.fit.cart_service.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.cart_service.event.PaymentResultEvent;
import vn.com.iuh.fit.cart_service.service.CartService;

@Slf4j
@Service
public class CartConsumer {

    private final CartService cartService;

    public CartConsumer(CartService cartService) {
        this.cartService = cartService;
    }

    @KafkaListener(
            topics = "payment-result-event",
            groupId = "cart-group-payment",
            containerFactory = "paymentResultListenerFactory"
    )
    public void processPaymentResultEvent(PaymentResultEvent event) {
        try {
            log.info("Nhận PaymentResultEvent từ Kafka trong CartService: {}", event);

            if (event.isSuccess()) {
                cartService.clearCartByUserId(event.getUserId());
                log.info(" Đã xoá giỏ hàng của userId: {}", event.getUserId());
            } else {
                log.warn(" Thanh toán thất bại - không xoá giỏ hàng userId: {}", event.getUserId());
            }

        } catch (Exception ex) {
            log.error(" Lỗi khi xử lý PaymentResultEvent trong CartService", ex);
        }
    }
}
