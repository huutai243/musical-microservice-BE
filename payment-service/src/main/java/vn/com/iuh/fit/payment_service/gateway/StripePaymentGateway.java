package vn.com.iuh.fit.payment_service.gateway;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.payment_service.dto.InternalPaymentRequestDTO;

import java.util.logging.Logger;

@Service
public class StripePaymentGateway implements PaymentGateway {

    private static final Logger log = Logger.getLogger(StripePaymentGateway.class.getName());

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @Override
    public boolean processPayment(InternalPaymentRequestDTO paymentRequest) {
        // Không dùng nữa vì Stripe Checkout là redirect-based
        throw new UnsupportedOperationException("Dùng generatePaymentUrl để thanh toán Stripe");
    }

    @Override
    public String generatePaymentUrl(InternalPaymentRequestDTO paymentRequest) {
        Stripe.apiKey = secretKey;

        try {
            long amountInCents = (long) (paymentRequest.getAmount() * 100);

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Order #" + paymentRequest.getOrderId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("orderId", String.valueOf(paymentRequest.getOrderId()))
                    .putMetadata("userId", paymentRequest.getUserId())
                    .build();

            Session session = Session.create(params);
            return session.getUrl();

        } catch (StripeException e) {
            log.severe("Lỗi khi tạo session thanh toán Stripe: " + e.getMessage());
            throw new RuntimeException("Không thể tạo session thanh toán Stripe", e);
        }
    }
}
