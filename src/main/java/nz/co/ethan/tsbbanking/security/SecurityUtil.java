package nz.co.ethan.tsbbanking.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

@Slf4j
public final class SecurityUtil {

    private SecurityUtil() {}

    public static Long currentUserId() {
        var ctx = SecurityContextHolder.getContext();
        var auth = (ctx == null) ? null : ctx.getAuthentication();
        if (auth == null) throw new IllegalStateException("No authenticated user found");

        Object principal = auth.getPrincipal();

        if (principal instanceof Long l) return l;

        if (principal instanceof String s && isDigits(s)) return Long.valueOf(s);

        if (principal instanceof java.util.Map<?,?> m) {
            Object uid = m.get("userId");
            if (uid != null && isDigits(uid.toString())) return Long.valueOf(uid.toString());
        }

        try {
            var m = principal.getClass().getMethod("getUserId");
            Object uid = m.invoke(principal);
            if (uid != null && isDigits(uid.toString())) return Long.valueOf(uid.toString());
        } catch (NoSuchMethodException ignore) {
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        try {
            var f = principal.getClass().getDeclaredField("userId");
            f.setAccessible(true);
            Object uid = f.get(principal);
            if (uid != null && isDigits(uid.toString())) return Long.valueOf(uid.toString());
        } catch (NoSuchFieldException ignore) {
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        String name = auth.getName();
        if (isDigits(name)) return Long.valueOf(name);

        Object cred = auth.getCredentials();
        if (cred instanceof String token) {
            String sub = tryReadSubFromJwt(token);
            if (isDigits(sub)) return Long.valueOf(sub);
        }

        throw new IllegalStateException("Unsupported principal type: " + principal);
    }

    private static boolean isDigits(String s) {
        return s != null && !s.isBlank() && s.chars().allMatch(Character::isDigit);
    }

    private static String tryReadSubFromJwt(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return null;
            String json = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), java.nio.charset.StandardCharsets.UTF_8);
            int i = json.indexOf("\"sub\"");
            if (i < 0) return null;
            int colon = json.indexOf(':', i);
            int quote1 = json.indexOf('"', colon + 1);
            int quote2 = json.indexOf('"', quote1 + 1);
            return (quote1 > 0 && quote2 > quote1) ? json.substring(quote1 + 1, quote2) : null;
        } catch (Exception e) {
            return null;
        }
    }

}
