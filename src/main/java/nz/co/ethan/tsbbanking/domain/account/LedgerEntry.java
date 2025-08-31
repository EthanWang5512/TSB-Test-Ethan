package nz.co.ethan.tsbbanking.domain.account;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import nz.co.ethan.tsbbanking.domain.enums.EntryType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@TableName("ledger_entries")
public class LedgerEntry {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("transfer_id")
    private Long transferId;

    @TableField("account_id")
    private Long accountId;

    @TableField("direction")
    private String type; // DEBIT / CREDIT

    private BigDecimal amount;

    private String currency;

    private String reference;

    @TableField("created_at")
    private OffsetDateTime createdAt;
}

