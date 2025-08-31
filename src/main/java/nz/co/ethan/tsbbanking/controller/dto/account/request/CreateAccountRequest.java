package nz.co.ethan.tsbbanking.controller.dto.account.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class CreateAccountRequest {
    @NotNull
    private Long customerId;

    @NotBlank
    @Schema(description = "nike name", example = "Personal")
    private String nickname;

    @NotNull
    @Schema(description = "'STREAMLINE','SAVINGS_ON_CALL','BUSINESS','FOREIGN_CURRENCY_CALL','TERM_DEPOSIT'",
            example = "STREAMLINE")
    private String accountType;

    @Schema(description = "Account owner role: 'PRIMARY','JOINT','TRUSTEE'", example = "PRIMARY")
    private String role; // "PRIMARY" Default

    @Schema(description = "Currency", example = "NZD")
    private String currency; // Default "NZD"

    @Schema(description = "Statement delivery", example = "true")
    private Boolean paperDelivery;
}



