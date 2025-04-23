package vn.com.iuh.fit.payment_service.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.iuh.fit.payment_service.service.PaymentService;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final PaymentService paymentService;

    @PostMapping("/webhook")
    @Transactional
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            log.info(" Nhận sự kiện Stripe webhook: {}", event.getType());

            if ("checkout.session.completed".equals(event.getType())) {
                Object dataObj = event.getData().getObject();

                if (!(dataObj instanceof Session)) {
                    log.error(" Không phải Session object: {}", dataObj.getClass().getSimpleName());
                    return ResponseEntity.badRequest().body("Unexpected object type");
                }
                Session session = (Session) dataObj;

                log.info("Session ID: {}", session.getId());
                log.info(" Metadata: {}", session.getMetadata());

                // 3. Lấy metadata orderId, userId
                String orderIdRaw = session.getMetadata().get("orderId");
                String userId    = session.getMetadata().get("userId");

                if (orderIdRaw == null || userId == null) {
                    log.error(" Thiếu metadata orderId/userId");
                    return ResponseEntity.badRequest().body("Missing metadata");
                }

                Long orderId;
                try {
                    orderId = Long.parseLong(orderIdRaw);
                } catch (NumberFormatException ex) {
                    log.error(" orderId không hợp lệ: {}", orderIdRaw);
                    return ResponseEntity.badRequest().body("Invalid orderId");
                }

                // 4. Kiểm tra payment_status của session
                //    - "paid"  => thành công
                //    - khác    => thất bại
                String paymentStatus = session.getPaymentStatus();
                if ("paid".equals(paymentStatus)) {
                    paymentService.confirmPaymentSuccess(orderId, userId);
                    log.info(" Payment SUCCEEDED cho Order {}", orderId);
                } else {
                    paymentService.confirmPaymentFailed(orderId, userId);
                    log.info(" Payment FAILED cho Order {}", orderId);
                }
            }

            return ResponseEntity.ok("Webhook processed");
        }
        catch (SignatureVerificationException sigEx) {
            log.error(" Xác thực chữ ký thất bại", sigEx);
            return ResponseEntity.status(400).body("Invalid signature");
        }
        catch (Exception ex) {
            log.error(" Lỗi khi xử lý webhook", ex);
            return ResponseEntity.status(500).body("Webhook error");
        }
    }
}
