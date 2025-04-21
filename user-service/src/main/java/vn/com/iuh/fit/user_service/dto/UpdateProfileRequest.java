package vn.com.iuh.fit.user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {
    private String avatarUrl; // URL của avatar
    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @JsonProperty("address")
    private String address;
}