package nz.co.ethan.tsbbanking.domain.account;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import nz.co.ethan.tsbbanking.domain.enums.TransferStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@TableName("transfers")
public class Transfer {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("from_account_id")
    private Long fromAccountId;

    @TableField("to_account_id")
    private Long toAccountId;

    private String currency;

    private BigDecimal amount;

    private String reference;

    @TableField("client_request_id")
    private String clientRequestId;


    private String status;

    @TableField("created_at")
    private OffsetDateTime createdAt;

    @TableField("created_by_user_id")
    private Long createdByUserId;


    @TableField(exist = false)
    private BigDecimal fromBalanceAfter;
    @TableField(exist = false)
    private BigDecimal toBalanceAfter;
}

