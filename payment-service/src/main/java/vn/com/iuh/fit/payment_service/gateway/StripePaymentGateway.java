package vn.com.iuh.fit.payment_service.gateway;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
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

        Map<String, Object> chargeParams = new HashMap<>();
        chargeParams.put("amount", (int) (paymentRequest.getAmount() * 100));
        chargeParams.put("currency", "usd");
        chargeParams.put("source", "tok_visa");
        chargeParams.put("description", "Thanh toán đơn hàng #" + paymentRequest.getOrderId());

        try {
            Charge charge = Charge.create(chargeParams);
            return charge.getPaid();
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
