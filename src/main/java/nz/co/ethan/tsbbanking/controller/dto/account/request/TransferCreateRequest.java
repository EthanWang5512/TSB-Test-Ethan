package nz.co.ethan.tsbbanking.controller.dto.account.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferCreateRequest {
    @NotNull
    @Schema(description = "accountNumber", example = "01-0980-0114667-19")
    private String fromAccountNumber;

    @NotNull
    @Schema(description = "accountNumber", example = "01-1503-7127842-14")
    private String toAccountNumber;

    @NotNull @DecimalMin(value = "0.01")
    @Digits(integer = 17, fraction = 2)
    @Schema(description = "accountNumber", example = "50.00")
    private BigDecimal amount;

    @Schema(description = "currency", example = "NZD")
    private String currency = "NZD";

    @Size(max = 18)
    @Schema(description = "reference", example = "Split bill")
    private String reference;

    @Size(max = 64)
    @Schema(description = "Client-generated unique request ID. Used as an idempotency key to prevent duplicate transfers.",
            example = "Client-generated unique request ID, input a random string")
    private String clientRequestId;
}
