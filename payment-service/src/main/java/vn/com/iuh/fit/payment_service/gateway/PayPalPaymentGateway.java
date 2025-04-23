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

    @Value("${paypal.success-url}")
    private String successUrl;

    @Value("${paypal.cancel-url}")
    private String cancelUrl;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean processPayment(InternalPaymentRequestDTO paymentRequest) {
        throw new UnsupportedOperationException("Sử dụng generatePaymentUrl để xử lý thanh toán PayPal.");
    }

    @Override
    public String generatePaymentUrl(InternalPaymentRequestDTO paymentRequest) {
        try {
            // 1. Lấy access token từ PayPal
            String accessToken = getAccessToken();

            // 2. Tạo order PayPal với các đường dẫn redirect
            String requestBody = "{\n" +
                    "  \"intent\": \"CAPTURE\",\n" +
                    "  \"purchase_units\": [\n" +
                    "    {\n" +
                    "      \"amount\": {\n" +
                    "        \"currency_code\": \"USD\",\n" +
                    "        \"value\": \"" + paymentRequest.getAmount() + "\"\n" +
                    "      },\n" +
                    "      \"custom_id\": \"" + paymentRequest.getOrderId() + "\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"application_context\": {\n" +
                    "    \"return_url\": \"" + successUrl + "?orderId=" + paymentRequest.getOrderId() + "\",\n" +
                    "    \"cancel_url\": \"" + cancelUrl + "?orderId=" + paymentRequest.getOrderId() + "\"\n" +
                    "  }\n" +
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

            // 3. Trích xuất URL để redirect user (rel="approve")
            JsonNode json = objectMapper.readTree(response.body());
            JsonNode links = json.get("links");

            for (JsonNode link : links) {
                if ("approve".equals(link.get("rel").asText())) {
                    return link.get("href").asText();
                }
            }

            throw new RuntimeException("Không tìm thấy URL approve từ PayPal.");

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo URL thanh toán PayPal", e);
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
}
