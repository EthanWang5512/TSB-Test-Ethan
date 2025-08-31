package nz.co.ethan.tsbbanking.controller.dto.account.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ListCustomerAccountsQuery {

    @NotNull
    private Long customerId;

    @Schema(description = "'STREAMLINE','SAVINGS_ON_CALL','BUSINESS','FOREIGN_CURRENCY_CALL','TERM_DEPOSIT'",
            example = "null")
    private String type;

    @Schema(description = "'ACTIVE','FROZEN','CLOSED'",
            example = "null")
    private String status;

    private Integer page = 1;
    private Integer size = 20;
}
