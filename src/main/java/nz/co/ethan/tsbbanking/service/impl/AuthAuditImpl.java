package nz.co.ethan.tsbbanking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nz.co.ethan.tsbbanking.domain.enums.AuthEventType;
import nz.co.ethan.tsbbanking.domain.user.AuthEvent;
import nz.co.ethan.tsbbanking.mapper.userAuth.AuthEventMapper;
import nz.co.ethan.tsbbanking.service.AuthAudit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthAuditImpl implements AuthAudit {

    private final AuthEventMapper authEventMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, AuthEventType type, String ip, String ua, Map<String,Object> meta) {
        try {
            AuthEvent e = new AuthEvent();
            e.setUserId(userId);          // 允许为 null（登录失败场景）
            e.setEventType(type.toString());
            e.setIpAddress(ip);           // 你的 INET TypeHandler 会处理
            e.setUserAgent(ua);
            e.setMeta(meta);
            authEventMapper.insert(e);
        } catch (Exception ex) {
            // 审计失败不阻断登录/业务
            log.warn("Auth audit failed: type={}, userId={}, ip={}, ua={}", type, userId, ip, ua, ex);
        }
    }
}