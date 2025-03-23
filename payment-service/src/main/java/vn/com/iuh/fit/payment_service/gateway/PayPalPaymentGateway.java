package vn.com.iuh.fit.payment_service.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.payment_service.dto.InternalPaymentRequestDTO;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
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

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean processPayment(InternalPaymentRequestDTO paymentRequest) {
        try {
            // 1. Lấy access token từ PayPal
            String accessToken = getAccessToken();

            // 2. Tạo đơn hàng trên PayPal
            String orderId = createPayPalOrder(paymentRequest, accessToken);

            // 3. Capture đơn hàng
            return capturePayPalOrder(orderId, accessToken);

        } catch (Exception e) {
            log.severe("Lỗi khi xử lý thanh toán PayPal: " + e.getMessage());
            return false;
        }
    }

    private String getAccessToken() throws IOException, InterruptedException {
        String credentials = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/oauth2/token"))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Không thể lấy access token từ PayPal: " + response.body());
        }

        JsonNode json = objectMapper.readTree(response.body());
        return json.get("access_token").asText();
    }

    private String createPayPalOrder(InternalPaymentRequestDTO dto, String accessToken) throws IOException, InterruptedException {
        String requestBody = "{\n" +
                "  \"intent\": \"CAPTURE\",\n" +
                "  \"purchase_units\": [\n" +
                "    {\n" +
                "      \"amount\": {\n" +
                "        \"currency_code\": \"USD\",\n" +
                "        \"value\": \"" + dto.getAmount() + "\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v2/checkout/orders"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201) {
            throw new RuntimeException("Tạo đơn hàng PayPal thất bại: " + response.body());
        }

        JsonNode json = objectMapper.readTree(response.body());
        return json.get("id").asText();
    }

    private boolean capturePayPalOrder(String orderId, String accessToken) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v2/checkout/orders/" + orderId + "/capture"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Capture PayPal Order Response: " + response.body());

        if (response.statusCode() != 201) {
            log.warning("Capture thất bại với mã: " + response.statusCode());
            return false;
        }

        JsonNode json = objectMapper.readTree(response.body());
        return json.get("status").asText().equals("COMPLETED");
    }

    @Override
    public String generatePaymentUrl(InternalPaymentRequestDTO paymentRequest) {
        return baseUrl + "/checkout?amount=" + paymentRequest.getAmount() + "&orderId=" + paymentRequest.getOrderId();
    }
}
