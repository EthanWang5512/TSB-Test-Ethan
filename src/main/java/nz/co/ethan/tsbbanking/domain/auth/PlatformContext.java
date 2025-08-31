package nz.co.ethan.tsbbanking.domain.auth;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
@Builder
public class PlatformContext {
    private List<String> roles;
    private Set<String> perms;
    private Long staffUserId;
    private String staffFirstName;
    private Boolean staffActive;
}
