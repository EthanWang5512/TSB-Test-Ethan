package nz.co.ethan.tsbbanking.controller.dto.userAuth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAdminUserRequest {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String phoneE164;
    @NotBlank
    private String roleCode;
}
