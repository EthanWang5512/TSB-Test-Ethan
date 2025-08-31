package nz.co.ethan.tsbbanking.controller.dto.userAuth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {
    @Email
    @NotBlank
    @Schema(description = "Register email", example = "test@gmail.com")
    private String email;

    @NotBlank
    @Pattern(regexp = "^[+0-9 ()-]{6,20}$", message = "Invalid phone format")
    @Schema(description = "Register phone", example = "022111222")
    private String phone;
}
