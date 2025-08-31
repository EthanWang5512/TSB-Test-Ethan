package nz.co.ethan.tsbbanking.domain.customer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("customers")
public class Customer {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String firstName;
    private String lastName;
    private String middleName;
    private String preferredName;
    private String email;
    private String contactNumber;
    private String addressLine1;
    private String addressLine2;
    private String suburb;
    private String city;
    private String region;
    private String postcode;
    private String country;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

