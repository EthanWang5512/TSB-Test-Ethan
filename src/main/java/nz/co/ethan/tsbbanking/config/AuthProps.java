package nz.co.ethan.tsbbanking.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthProps {

    private boolean staffMfaEnabled = false;
    private boolean customerMfaEnabled = false;

    private long accessTtlSecondsPlatform = 3600;   // 1h
    private long accessTtlSecondsRetail   = 3600;   // 1h
    private long refreshTtlDays           = 30;     // 30d
    @Data
    public static class Jwt {
        private String hs256Secret;
        private long accessTtlSeconds;
        private String issuer = "tsb-banking";
        private String audiencePlatform = "platform";
        private String audienceRetail = "retail";
    }
    @Data
    public static class Session {
        private int refreshTtlDays;
    }
    private Jwt jwt = new Jwt();
    private Session session = new Session();
}
