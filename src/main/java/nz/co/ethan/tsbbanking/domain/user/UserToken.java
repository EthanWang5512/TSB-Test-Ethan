package nz.co.ethan.tsbbanking.domain.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName(value = "user_tokens", autoResultMap = true)
public class UserToken {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField(value = "purpose")
    private String purpose;

    @TableField("token_hash")
    private String tokenHash;

    @TableField("expires_at")
    private OffsetDateTime expiresAt;

    @TableField("used_at")
    private OffsetDateTime usedAt;

    @TableField("created_at")
    private OffsetDateTime createdAt;
}
