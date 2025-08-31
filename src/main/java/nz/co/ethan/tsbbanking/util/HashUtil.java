package nz.co.ethan.tsbbanking.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public class HashUtil {

    public static String hashTo8Digits(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            var digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            var bi = new BigInteger(1, digest);
            long mod = bi.mod(BigInteger.valueOf(100_000_000L)).longValue();
            return String.format("%08d", mod);
        } catch (Exception e) {
            throw new IllegalStateException("hash error", e);
        }
    }
}
