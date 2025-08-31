package nz.co.ethan.tsbbanking.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class FlywayDevResetConfig {

    @Bean
    public FlywayMigrationStrategy strategy() {
        System.out.println(">>> [FlywayDevResetConfig] strategy bean loaded (profile=dev)");
        return (Flyway flyway) -> {
            try {
                String reset = System.getenv("DEV_RESET_DB");
                if ("true".equalsIgnoreCase(reset)) {
                    System.out.println(">>> DEV_RESET_DB=true -> clean + migrate");
                    flyway.clean();
                }
                flyway.migrate(); // 先尝试 migrate
            } catch (FlywayValidateException ex) {
                System.out.println(">>> Flyway validate failed -> auto clean + migrate (dev only)");
                flyway.clean();
                flyway.migrate();
            }
        };
    }
}
