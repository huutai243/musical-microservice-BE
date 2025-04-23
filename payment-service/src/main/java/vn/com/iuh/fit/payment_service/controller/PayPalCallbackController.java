package vn.com.iuh.fit.payment_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.iuh.fit.payment_service.service.PaymentService;

@Slf4j
@RestController
@RequestMapping("/api/payment/paypal")
@RequiredArgsConstructor
public class PayPalCallbackController {

    private final PaymentService paymentService;

    /**
     *  Người dùng thanh toán thành công trên PayPal
     * Sau khi người dùng đồng ý thanh toán, PayPal redirect về URL này.
     * FE nên lấy orderId + userId từ query để gọi BE xác nhận trạng thái đơn hàng.
     */
    @GetMapping("/success")
    public ResponseEntity<String> handlePayPalSuccess(
            @RequestParam("orderId") Long orderId,
            @RequestParam("userId") String userId
    ) {
        log.info(" PayPal redirect thành công: orderId={}, userId={}", orderId, userId);
        paymentService.confirmPaymentSuccess(orderId, userId);
        return ResponseEntity.ok("Thanh toán PayPal thành công!");
    }

    /**
     * Người dùng bấm Cancel trên PayPal → redirect về đây
     */
    @GetMapping("/cancel")
    public ResponseEntity<String> handlePayPalCancel(
            @RequestParam(value = "orderId", required = false) Long orderId
    ) {
        log.warn(" Người dùng hủy thanh toán PayPal. OrderId={}", orderId);
        return ResponseEntity.ok("Thanh toán PayPal đã bị hủy.");
    }
}
