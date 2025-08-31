package nz.co.ethan.tsbbanking.domain.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import nz.co.ethan.tsbbanking.config.typehandler.InetTypeHandler;
import nz.co.ethan.tsbbanking.config.typehandler.JsonbTypeHandler;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@TableName(value = "auth_events", autoResultMap = true)
public class AuthEvent {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField(value = "event_type")
    private String eventType;

    @TableField(value = "ip_address", typeHandler = InetTypeHandler.class)
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField("created_at")
    private OffsetDateTime createdAt;

    @TableField(value = "meta", typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> meta;
}
