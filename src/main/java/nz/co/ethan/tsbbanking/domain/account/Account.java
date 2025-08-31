package nz.co.ethan.tsbbanking.domain.account;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@TableName("accounts")
public class Account {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String accountNumber;

    private String nickname;

    @TableField(value = "account_type")
    private String accountType;

    private String currency;     // e.g. "NZD"

    private String status;

    private BigDecimal balance;

    private BigDecimal overdraftLimit;

    private Boolean paperDelivery;

    @TableField("resident_withholding_tax_rate")
    private BigDecimal residentWithholdingTaxRate;

    @Version
    private Long version;

    @TableField("open_at")
    private OffsetDateTime openAt;

    private OffsetDateTime updatedAt;
}
