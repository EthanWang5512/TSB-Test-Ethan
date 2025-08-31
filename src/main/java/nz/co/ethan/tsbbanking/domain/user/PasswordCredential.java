package nz.co.ethan.tsbbanking.domain.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@TableName("password_credentials")
@Data
public class PasswordCredential {

    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    private String passwordHash;

    private String passwordAlgo;

    private OffsetDateTime passwordUpdatedAt;

    private Integer failedLoginCount;

    private OffsetDateTime lockedUntil;
}
