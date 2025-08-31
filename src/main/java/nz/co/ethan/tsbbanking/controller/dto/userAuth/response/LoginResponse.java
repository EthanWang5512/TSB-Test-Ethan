package nz.co.ethan.tsbbanking.controller.dto.userAuth.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data @AllArgsConstructor @Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private long   expiresIn;


}