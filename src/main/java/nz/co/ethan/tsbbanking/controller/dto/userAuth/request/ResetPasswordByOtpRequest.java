package nz.co.ethan.tsbbanking.controller.dto.userAuth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordByOtpRequest {
    @NotBlank
    @Schema(description = "Register phone", example = "022111222")
    private String phoneE164;

    @NotBlank
    @Pattern(regexp = "^\\d{6}$")
    @Schema(description = "Get it from request-rest-password-otp API", example = "123456")
    private String otp;

    @NotBlank
    @Schema(description = "New Password", example = "123456")
    private String newPassword;
}
