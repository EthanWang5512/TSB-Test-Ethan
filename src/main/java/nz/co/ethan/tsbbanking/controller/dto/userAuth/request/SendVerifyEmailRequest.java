package nz.co.ethan.tsbbanking.controller.dto.userAuth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendVerifyEmailRequest {
    @Email @NotBlank
    @Schema(description = "Verify email for new user", example = "test@gmail.com")
    private String email;

    @NotBlank
    @Schema(description = "Get it from register API", example = "12345678")
    private String username;
}