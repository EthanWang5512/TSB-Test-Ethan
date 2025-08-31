package nz.co.ethan.tsbbanking.controller.dto.customer.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BindCustomerUserRequest {

    @NotNull
    @Schema(description = "Get it from creat customer API", example = "1")
    private Long customerId;

    @NotBlank
    @Schema(description = "username 8 digital number", example = "")
    private String username;
    @NotBlank
    @Schema(description = "User permission 'VIEW_ONLY','TRANSACT','ADMIN'", example = "ADMIN")
    private String role;      // Check in 'VIEW_ONLY','TRANSACT','ADMIN'
}
