package nz.co.ethan.tsbbanking.controller.dto.account.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountSummary {
    private Long accountId;
    private String accountNumber;
    private String nickname;
    private String accountType;
    private String currency;
    private String status;
    private BigDecimal balance;
    private BigDecimal overdraftLimit;
    private BigDecimal residentWithholdingTaxRate;
    private Boolean primaryOwner;
    private String ownerRole;        // PRIMARY / SECONDARY / SIGNATORY
    private OffsetDateTime openedAt;
}
