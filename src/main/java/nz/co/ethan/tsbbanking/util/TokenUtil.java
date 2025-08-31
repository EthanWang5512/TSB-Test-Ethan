package nz.co.ethan.tsbbanking.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

public class TokenUtil {
    private static final SecureRandom RNG = new SecureRandom();

    public static String random() {
        return randomUrlToken(32);
    }

    public static String randomUrlToken(int bytes) {
        byte[] b = new byte[bytes];
        RNG.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    public static String signJwtHs256(String subject, long expiresSeconds, byte[] secret) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiresSeconds)))
                .signWith(Keys.hmacShaKeyFor(secret))
                .compact();
    }

    public static String sha256Base64(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(s.getBytes()));
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
    }
}

