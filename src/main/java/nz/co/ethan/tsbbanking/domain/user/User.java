package nz.co.ethan.tsbbanking.domain.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String email;

    @TableField("phone_e164")
    private String phoneE164;

    @TableField("is_email_verified")
    private Boolean isEmailVerified;

    @TableField("is_phone_verified")
    private Boolean isPhoneVerified;

    @TableField(value = "status")
    private String status;

    @TableField("created_at")
    private OffsetDateTime createdAt;

    @TableField("updated_at")
    private OffsetDateTime updatedAt;

    @TableField("last_login_at")
    private OffsetDateTime lastLoginAt;
}
