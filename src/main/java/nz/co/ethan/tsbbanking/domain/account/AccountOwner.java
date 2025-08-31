package nz.co.ethan.tsbbanking.domain.account;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("account_owners")
public class AccountOwner {

    @TableField("account_id")  private Long accountId;
    @TableField("customer_id") private Long customerId;
    @TableField("owner_role")        private String role;      // "PRIMARY" / "SECONDARY" / "SIGNATORY"
    @TableField("created_at")    private OffsetDateTime createdAt;
}
