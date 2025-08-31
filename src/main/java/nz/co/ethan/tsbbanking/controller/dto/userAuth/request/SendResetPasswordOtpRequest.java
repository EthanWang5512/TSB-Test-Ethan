package nz.co.ethan.tsbbanking.controller.dto.userAuth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendResetPasswordOtpRequest {
    @NotBlank
    @Schema(description = "Reset password by otp", example = "022111222")
    private String phoneE164;
}
