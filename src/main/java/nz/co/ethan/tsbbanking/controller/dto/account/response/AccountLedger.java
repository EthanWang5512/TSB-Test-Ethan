package nz.co.ethan.tsbbanking.controller.dto.account.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
@Data
public class AccountLedger {
    private Long transferId;
    private String direction;                // DEBIT | CREDIT
    private BigDecimal amount;
    private String currency;
    private BigDecimal delta;                // signed amount: CREDIT=+amount, DEBIT=-amount
    private String status;                   // from transfers
    private String reference;                // transfers.description
    private OffsetDateTime postedAt;         // transfers.posted_at
    private OffsetDateTime createdAt;        // ledger_entries.created_at
    private String counterpartyAccountNumber;
}
