package nz.co.ethan.tsbbanking.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonUtil {
    private static final ObjectMapper M = new ObjectMapper();
    private JsonUtil() {}

    public static Map<String,Object> of(Object... kv) {
        if (kv == null || kv.length == 0) return Map.of();
        if ((kv.length & 1) == 1) throw new IllegalArgumentException("key-value not paired");
        Map<String,Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put(String.valueOf(kv[i]), kv[i+1]);
        return m;
    }
    public static String toJson(Object o) {
        try { return M.writeValueAsString(o); }
        catch (JsonProcessingException e) { throw new RuntimeException(e); }
    }
}

