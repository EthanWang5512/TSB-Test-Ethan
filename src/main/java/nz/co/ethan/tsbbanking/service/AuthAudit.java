package nz.co.ethan.tsbbanking.service;

import nz.co.ethan.tsbbanking.domain.enums.AuthEventType;

import java.util.Map;

public interface AuthAudit {
    void record(Long userId, AuthEventType type, String ip, String ua, Map<String,Object> meta);

    default void record(Long userId, AuthEventType type, String ip, String ua) {
        record(userId, type, ip, ua, Map.of());
    }
}
