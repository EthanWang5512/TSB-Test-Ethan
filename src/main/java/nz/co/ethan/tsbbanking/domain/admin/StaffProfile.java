package nz.co.ethan.tsbbanking.domain.admin;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("staff_profiles")
public class StaffProfile {
    @TableId
    private Long userId;

    private String employeeNo;
    private String branchCode;
    private String title;
    private String status;
    private OffsetDateTime createdAt;
}
