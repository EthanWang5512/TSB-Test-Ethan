package nz.co.ethan.tsbbanking.domain.customer;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("customer_users")
public class CustomerUser {
    private Long customerId;
    private Long userId;

    @TableField(value = "access_role")
    private String accessRole; // OWNER / ADMIN / DELEGATE / VIEW_ONLY

    private OffsetDateTime createdAt;
}

