package nz.co.ethan.tsbbanking.controller.dto.account.response;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class TransferResponse {
    private Long transferId;
    private String status; // PENDING/POSTED/FAILED
    private String currency;

    private Long fromAccountId;
    private Long toAccountId;

    private BigDecimal amount;

    private BigDecimal fromBalanceAfter;
    private BigDecimal toBalanceAfter;

    private String reference;
    private String clientRequestId;
    private OffsetDateTime createdAt;
}

