package nz.co.ethan.tsbbanking.controller.dto.userAuth.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class StaffLoginResponse {
    private String accessToken;
    private String refreshToken;
    private int    expiresIn;
    private String tokenType;
    private Map<String, Object> user;
}