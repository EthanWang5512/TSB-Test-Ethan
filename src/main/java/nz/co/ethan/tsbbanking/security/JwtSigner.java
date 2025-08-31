package nz.co.ethan.tsbbanking.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nz.co.ethan.tsbbanking.config.AuthProps;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static java.util.Objects.requireNonNull;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtSigner {

    private final AuthProps props;
    private volatile SecretKey key;

    private SecretKey getKey() {
        SecretKey k = key;
        if (k == null) {
            synchronized (this) {
                if (key == null) {
                    String secret = requireNonNull(props.getJwt()).getHs256Secret();
                    key = buildKey(secret);
                }
                k = key;
            }
        }
        return k;
    }

    public String sign(Map<String, Object> claims, long ttlSeconds) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlSeconds * 1000))
                .signWith(getKey(), Jwts.SIG.HS256)   // 0.12.5 写法
                .compact();
    }

    public Claims verify(String token) {
        try {
            var parser = Jwts.parser()
                    .verifyWith(getKey())
                    .clockSkewSeconds(60)
                    .build();

            Claims claims = parser.parseSignedClaims(token).getPayload();

            var jwtCfg = requireNonNull(props.getJwt());
            String expectedIss     = orDefault(jwtCfg.getIssuer(), "tsb-banking");

            String iss   = asString(claims.get("iss"));
            Object audOb = claims.get("aud");

            if (log.isDebugEnabled()) {
                log.debug("JWT claims: iss={}, aud={}", iss, audOb);
            }

            if (!equalsIgnoreCase(iss, expectedIss)) {
                throw new SecurityException("Bad issuer");
            }



            return claims;

        } catch (JwtException e) {
            throw e;
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("Invalid JWT", e);
        }
    }

    // ===== helpers =====

    private static SecretKey buildKey(String secret) {
        byte[] bytes = secret.startsWith("base64:")
                ? Base64.getDecoder().decode(secret.substring(7))
                : secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalStateException("HS256 secret must be >= 32 bytes");
        return Keys.hmacShaKeyFor(bytes);
    }

    private static String asString(Object v) { return v == null ? null : String.valueOf(v); }
    private static String orDefault(String v, String def) { return v == null ? def : v; }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }
}
