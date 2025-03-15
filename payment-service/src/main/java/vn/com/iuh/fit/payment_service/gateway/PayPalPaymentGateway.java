package vn.com.iuh.fit.payment_service.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.payment_service.dto.PaymentRequestDTO;

import java.util.logging.Logger;

@Service
public class PayPalPaymentGateway implements PaymentGateway {
    private static final Logger log = Logger.getLogger(PayPalPaymentGateway.class.getName());

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.base-url}")
    private String baseUrl;

    @Override
    public boolean processPayment(PaymentRequestDTO paymentRequest) {
        log.info(" Xử lý thanh toán qua PayPal cho đơn hàng #" + paymentRequest.getOrderId());
        return true;
    }

    @Override
    public String generatePaymentUrl(PaymentRequestDTO paymentRequest) {
        log.info(" Tạo URL thanh toán PayPal cho đơn hàng #" + paymentRequest.getOrderId());
        return baseUrl + "/checkout?amount=" + paymentRequest.getAmount();
    }
}
