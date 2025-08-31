package nz.co.ethan.tsbbanking.config;

import nz.co.ethan.tsbbanking.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
@EnableMethodSecurity
public class SecurityConfig {


    @Bean
    @Order(1)
    public SecurityFilterChain swaggerChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**")
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable()); // 文档端点可关 CSRF
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers(
                                "/api/users/reset-password",
                                "/api/users/login",
                                "/api/users/staff/login",
                                "/api/users/request-reset-password-otp",
                                "/api/users/reset-password-otp"
                        ).permitAll()

                        // Platform
                        .requestMatchers(
                                "/api/users/staff/logout",
                                "/api/users/register",
                                "/api/users/send-verify-email",
                                "/api/customers/register",
                                "/api/customers/users/bind",
                                "/api/account/create"
                        ).hasAuthority("SCOPE_PLATFORM")

                        // Retail
                        .requestMatchers(
                                "/api/users/logout",
                                "/api/account/customer/accounts",
                                "/api/account/transfer",
                                "/api/account/ledgers"
                        ).access((authz, ctx) ->
                                AuthorizationManagers.allOf(
                                        AuthorityAuthorizationManager.hasAuthority("SCOPE_RETAIL"),
                                        AuthorityAuthorizationManager.hasAuthority("CHANNEL_CUSTOMER")
                                ).check(authz, ctx)
                        )
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}

