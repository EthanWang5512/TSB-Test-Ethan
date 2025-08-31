package nz.co.ethan.tsbbanking.security;


import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nz.co.ethan.tsbbanking.domain.auth.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 解析并验证 JWT -> 构造通用 JwtPrincipal -> 塞入 SecurityContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtVerifier jwtVerifier;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {

        String token = resolveBearer(req.getHeader("Authorization"));


        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(req, resp);
            return;
        }
        if (!StringUtils.hasText(token)) {
            chain.doFilter(req, resp);
            return;
        }

        try {
            Claims c = jwtVerifier.verify(token);

            String scope = str(c.get("scope"));

            if (!"PLATFORM".equals(scope) && !"RETAIL".equals(scope)) throw new RuntimeException("bad scope");

            Long userId = asLong(c.get("sub"));
            String jti = str(c.get("jti"));
            String sid = str(c.get("sid"));

            JwtPrincipal principal;

            if ("RETAIL".equals(scope)) {

                Long activeCustomerId = asLong(c.get("customer_id"));
                String customerRole = str(c.get("customer_role"));
                Set<Long> boundCustomerIds = toLongSet(c.get("customer_ids"));

                RetailContext rc = RetailContext.builder()
                        .activeCustomerId(activeCustomerId)
                        .customerRole(customerRole)
                        .boundCustomerIds(boundCustomerIds)
                        .build();

                principal = JwtPrincipal.builder()
                        .userId(userId)
                        .scope(AuthScope.RETAIL)
                        .channel(AuthChannel.CUSTOMER)
                        .retail(rc)
                        .jwtId(jti)
                        .sessionId(sid)
                        .build();

            } else {

                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) c.getOrDefault("roles", List.of());
                Set<String> perms = toStringSet(c.get("perms"));

                PlatformContext pc = PlatformContext.builder()
                        .roles(roles)
                        .perms(perms)
                        .build();

                principal = JwtPrincipal.builder()
                        .userId(userId)
                        .scope(AuthScope.PLATFORM)
                        .channel(AuthChannel.STAFF)
                        .platform(pc)
                        .jwtId(jti)
                        .sessionId(sid)
                        .build();
            }

            Collection<? extends GrantedAuthority> auths = principal.toAuthorities();
            log.debug("Authorities: {}", auths);
            var authentication = new UsernamePasswordAuthenticationToken(principal, token, auths);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.debug("JWT verification failed: {}", e.getMessage());
        }

        chain.doFilter(req, resp);
    }

    private static String resolveBearer(String authz) {
        if (authz == null || !authz.startsWith("Bearer ")) return null;
        String token = authz.substring(7).trim();
        return token.isEmpty() ? null : token;  // 空也返回 null
    }

    private static String str(Object v) { return v == null ? null : String.valueOf(v); }
    private static Long asLong(Object v) { return (v == null) ? null : Long.valueOf(String.valueOf(v)); }

    @SuppressWarnings("unchecked")
    private static Set<Long> toLongSet(Object v) {
        if (v == null) return Set.of();
        if (v instanceof Collection<?> col) {
            Set<Long> s = new LinkedHashSet<>();
            for (Object o : col) s.add(asLong(o));
            return s;
        }
        return Set.of(asLong(v));
    }

    @SuppressWarnings("unchecked")
    private static Set<String> toStringSet(Object v) {
        if (v == null) return Set.of();
        if (v instanceof Collection<?> col) {
            Set<String> s = new LinkedHashSet<>();
            for (Object o : col) s.add(str(o));
            return s;
        }
        return Set.of(str(v));
    }
}