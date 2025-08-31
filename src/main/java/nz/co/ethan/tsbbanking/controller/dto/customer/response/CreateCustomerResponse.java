package nz.co.ethan.tsbbanking.controller.dto.customer.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class CreateCustomerResponse {
    private Long id;
    private String displayName;
    private OffsetDateTime createdAt;
}
