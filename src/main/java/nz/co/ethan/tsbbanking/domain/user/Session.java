package nz.co.ethan.tsbbanking.domain.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import nz.co.ethan.tsbbanking.config.typehandler.InetTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.time.OffsetDateTime;

@Data
@TableName("sessions")
public class Session {
    @TableId(type = IdType.AUTO) private Long id;
    @TableField("user_id")       private Long userId;
    @TableField("refresh_token_hash") private String refreshTokenHash;
    @TableField("expires_at")    private OffsetDateTime expiresAt;
    @TableField("revoked_at")    private OffsetDateTime revokedAt;

    @TableField(value = "ip_address",
            jdbcType = JdbcType.OTHER,
            typeHandler = InetTypeHandler.class) private String ip;
    @TableField("scope")   private String scope;

    @TableField("user_agent")    private String userAgent;
    @TableField("created_at")    private OffsetDateTime createdAt;
}
