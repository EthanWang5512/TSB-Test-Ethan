package nz.co.ethan.tsbbanking.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI api() {
        // 1. Bearer access token
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        // 2. Refresh token header
        SecurityScheme refresh = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-Refresh-Token");

        // 3. Components
        Components components = new Components()
                .addSecuritySchemes("bearerAuth", bearer)
                .addSecuritySchemes("refreshAuth", refresh);

        SecurityRequirement global = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("TSB Banking Demo API")
                        .version("v1")
                        .description("Internal banking demo API"))
                .components(components)
                .addSecurityItem(global)
                .addServersItem(new Server().url("/"));
    }
}
