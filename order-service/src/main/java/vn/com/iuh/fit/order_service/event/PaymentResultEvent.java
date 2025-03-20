package vn.com.iuh.fit.order_service.event;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultEvent {
    private Long orderId;
    private boolean success;
    private String message;
}

