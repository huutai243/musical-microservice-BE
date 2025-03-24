package vn.com.iuh.fit.iuh.notification_service.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequest {
    private String to;
    private String subject;
    private String body;
}
