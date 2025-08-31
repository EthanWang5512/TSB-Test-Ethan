package nz.co.ethan.tsbbanking.controller.dto.account.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ListAccountLedgerRequest {

    @NotNull
    @Schema(description = "accountNumber", example = "01-0980-0114667-19")
    private String accountNumber;
    private Integer page = 1;
    private Integer size = 20;
}
