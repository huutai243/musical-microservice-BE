package vn.com.iuh.fit.payment_service.gateway;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.payment_service.dto.InternalPaymentRequestDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class StripePaymentGateway implements PaymentGateway {

    private static final Logger log = Logger.getLogger(StripePaymentGateway.class.getName());

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Override
    public boolean processPayment(InternalPaymentRequestDTO paymentRequest) {
        Stripe.apiKey = secretKey;

        Map<String, Object> paymentParams = new HashMap<>();
        paymentParams.put("amount", paymentRequest.getAmount().longValue());
        paymentParams.put("currency", "vnd");
        // Thành công (Visa)
        paymentParams.put("payment_method", "pm_card_visa");
        // Thành công (Mastercard)
        paymentParams.put("payment_method", "pm_card_mastercard");
        // Thẻ bị từ chối
        paymentParams.put("payment_method", "pm_card_chargeDeclined");
        // Thiếu tiền
        paymentParams.put("payment_method", "pm_card_insufficientFunds");
       // Lỗi hệ thống
        paymentParams.put("payment_method", "pm_card_systemFailure");

        paymentParams.put("confirm", true);
        paymentParams.put("description", "Thanh toán đơn hàng #" + paymentRequest.getOrderId());

        try {
            PaymentIntent paymentIntent = PaymentIntent.create(paymentParams);
            log.info("Stripe response status: " + paymentIntent.getStatus());
            return "succeeded".equals(paymentIntent.getStatus());
        } catch (StripeException e) {
            log.severe("Lỗi khi xử lý thanh toán Stripe: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String generatePaymentUrl(InternalPaymentRequestDTO paymentRequest) {
        return null; // Not used for Stripe
    }
}
