package nz.co.ethan.tsbbanking.controller.dto.userAuth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SetPasswordByTokenRequest {
    @NotBlank
    @Schema(description = "Git it from console log [Moke EMAIL]", example = "uQp-oNZ6Aj7caS7WmixBscrV95aNsmfaWn1PUNUL23M")
    private String token;
    @NotBlank
    @Schema(description = "New Password", example = "123456")
    private String newPassword;
}