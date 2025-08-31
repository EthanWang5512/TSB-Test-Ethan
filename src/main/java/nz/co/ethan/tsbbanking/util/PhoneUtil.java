package nz.co.ethan.tsbbanking.util;

public class PhoneUtil {

    public static String normalizeNzPhone(String raw) {
        String d = raw.replaceAll("[^0-9+]", "");
        if (d.startsWith("+")) return d;
        if (d.startsWith("0")) return "+64" + d.replaceFirst("^0+", "");
        return "+64" + d;
    }
}
