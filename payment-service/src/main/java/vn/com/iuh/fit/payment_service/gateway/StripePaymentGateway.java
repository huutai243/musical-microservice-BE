package vn.com.iuh.fit.payment_service.gateway;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.payment_service.dto.PaymentRequestDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class StripePaymentGateway implements PaymentGateway {

    private static final Logger log = Logger.getLogger(StripePaymentGateway.class.getName());

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Override
    public boolean processPayment(PaymentRequestDTO paymentRequest) {
        Stripe.apiKey = secretKey;

        Map<String, Object> paymentParams = new HashMap<>();
        paymentParams.put("amount", (int) (paymentRequest.getAmount() * 100)); // Cents
        paymentParams.put("currency", "usd");
        paymentParams.put("payment_method", "pm_card_visa");
        paymentParams.put("confirm", true);
        paymentParams.put("description", "Thanh toán đơn hàng #" + paymentRequest.getOrderId());

        try {
            PaymentIntent paymentIntent = PaymentIntent.create(paymentParams);
            return "succeeded".equals(paymentIntent.getStatus());
        } catch (StripeException e) {
            log.severe(" Lỗi khi xử lý thanh toán qua Stripe: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String generatePaymentUrl(PaymentRequestDTO paymentRequest) {
        return null;
    }
}
