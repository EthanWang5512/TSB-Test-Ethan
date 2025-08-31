package nz.co.ethan.tsbbanking.domain.auth;

import lombok.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import java.util.*;

@Getter @Builder
public class JwtPrincipal {
    private Long userId;
    private AuthScope scope;          // PLATFORM / RETAIL
    private AuthChannel channel;      // STAFF / CUSTOMER
    private String sessionId;
    private String jwtId;

    private PlatformContext platform;
    private RetailContext retail;

    /** 统一导出为 Spring Security 的 authorities，便于 @PreAuthorize 使用 */
    public Collection<? extends GrantedAuthority> toAuthorities() {
        List<GrantedAuthority> auths = new ArrayList<>();
        auths.add(new SimpleGrantedAuthority("SCOPE_" + scope.name()));     // SCOPE_PLATFORM / SCOPE_RETAIL
        auths.add(new SimpleGrantedAuthority("CHANNEL_" + channel.name())); // CHANNEL_STAFF / CHANNEL_CUSTOMER

        if (platform != null) {
            platform.getRoles().forEach(r -> auths.add(new SimpleGrantedAuthority("ROLE_" + r)));
            platform.getPerms().forEach(p -> auths.add(new SimpleGrantedAuthority("PERM_" + p)));
        }
        if (retail != null) {
            if (retail.getCustomerRole() != null) {
                auths.add(new SimpleGrantedAuthority("C_ROLE_" + retail.getCustomerRole()));
            }
            if (retail.getActiveCustomerId() != null) {
                auths.add(new SimpleGrantedAuthority("CID_" + retail.getActiveCustomerId()));
            }
        }
        return auths;
    }

    public boolean isPlatformStaff() {
        return scope == AuthScope.PLATFORM && channel == AuthChannel.STAFF && platform != null;
    }
    public boolean isRetailCustomer() {
        return scope == AuthScope.RETAIL && channel == AuthChannel.CUSTOMER && retail != null;
    }
}
