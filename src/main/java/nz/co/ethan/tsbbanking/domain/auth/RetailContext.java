package nz.co.ethan.tsbbanking.domain.auth;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class RetailContext {
    // customer_users
    private Long activeCustomerId;
    private String customerRole;         //  VIEW_ONLY / MAKER / APPROVER
    private Set<Long> boundCustomerIds;
}
