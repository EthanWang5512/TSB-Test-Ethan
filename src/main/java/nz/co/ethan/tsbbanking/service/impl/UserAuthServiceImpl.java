package nz.co.ethan.tsbbanking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nz.co.ethan.tsbbanking.common.BizException;
import nz.co.ethan.tsbbanking.common.ErrorCodes;
import nz.co.ethan.tsbbanking.config.AuthProps;
import nz.co.ethan.tsbbanking.controller.dto.userAuth.request.LoginRequest;
import nz.co.ethan.tsbbanking.controller.dto.common.APIResponse;
import nz.co.ethan.tsbbanking.controller.dto.userAuth.response.CreateAdminUserResponse;
import nz.co.ethan.tsbbanking.controller.dto.userAuth.response.LoginResponse;
import nz.co.ethan.tsbbanking.controller.dto.userAuth.response.StaffLoginResponse;
import nz.co.ethan.tsbbanking.domain.admin.StaffProfile;
import nz.co.ethan.tsbbanking.domain.enums.AuthEventType;
import nz.co.ethan.tsbbanking.domain.enums.UserRoleScope;
import nz.co.ethan.tsbbanking.domain.enums.UserStatus;
import nz.co.ethan.tsbbanking.domain.enums.UserTokenPurpose;
import nz.co.ethan.tsbbanking.domain.user.*;
import nz.co.ethan.tsbbanking.mapper.Admin.StaffProfileMapper;
import nz.co.ethan.tsbbanking.mapper.PlatformRoleQueryMapper;
import nz.co.ethan.tsbbanking.mapper.userAuth.*;
import nz.co.ethan.tsbbanking.security.JwtSigner;
import nz.co.ethan.tsbbanking.service.AuthAudit;
import nz.co.ethan.tsbbanking.service.UserAuthService;
import nz.co.ethan.tsbbanking.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static nz.co.ethan.tsbbanking.util.HashUtil.hashTo8Digits;
import static nz.co.ethan.tsbbanking.util.PhoneUtil.normalizeNzPhone;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthServiceImpl implements UserAuthService {

    private final UserMapper userMapper;
    private final UserTokenMapper tokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final MailSender mailSender;
    private final PasswordCredentialMapper passwordCredentialMapper;
    private final UserTokenMapper userTokenMapper;
    private final SessionMapper sessionMapper;
    private final AuthEventMapper authEventMapper;
    private final StaffProfileMapper staffProfileMapper;
    private final PlatformRoleQueryMapper platformRoleQueryMapper;
    private final JwtSigner jwtSigner;
    private final AuthProps authProps;
    private final SmsSender smsSender;
    private final AuthAudit authAudit;

    @Override
    public User registerUser(String emailRaw, String phoneRaw) {

        final String email = emailRaw.trim().toLowerCase(Locale.ROOT);
        final String phoneE164 = "+64" + phoneRaw;

        User exists = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .or()
                .eq(User::getPhoneE164, phoneE164));
        if (exists != null)
            throw BizException.error(ErrorCodes.USER_EXISTS.code(), "User already exists");

        // Generate unique username
        String base = email + "|" + phoneE164;
        String username = hashTo8Digits(base);
        if (usernameTaken(username)) {
            for (int i = 1; i <= 999; i++) {
                String candidate = hashTo8Digits(base + "|" + i);
                if (!usernameTaken(candidate)) {
                    username = candidate;
                    break;
                }
            }
        }

        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPhoneE164(phoneE164);
        u.setStatus(UserStatus.ACTIVE.toString());
        u.setIsEmailVerified(false);
        u.setIsPhoneVerified(false);
        u.setCreatedAt(OffsetDateTime.now());
        u.setUpdatedAt(OffsetDateTime.now());

        userMapper.insert(u);

        PasswordCredential p = new PasswordCredential();
        p.setUserId(u.getId());

        passwordCredentialMapper.insert(p);

        return u;
    }

    @Override
    @Transactional
    public void sendVerifyEmailAndSetPasswordLink(String emailRaw, String username, String origin) {
        String email = emailRaw.trim().toLowerCase();
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email)
                                                                .eq(User::getUsername, username));
        if (u == null)
            throw BizException.error(ErrorCodes.USER_NOT_FOUND.code(), "email not found");

        String raw = CryptoUtil.randomUrlSafeToken(32);
        String hash = CryptoUtil.sha256(raw);

        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneId.of("Pacific/Auckland")).plus(24, ChronoUnit.HOURS);

        // Delete expire and same type token
        tokenMapper.delete(new LambdaQueryWrapper<UserToken>()
                .eq(UserToken::getUserId, u.getId())
                .eq(UserToken::getPurpose, UserTokenPurpose.EMAIL_VERIFY_AND_SET_PASSWORD.toString())
                .isNull(UserToken::getUsedAt));

        UserToken t = new UserToken();
        t.setUserId(u.getId());
        t.setPurpose(UserTokenPurpose.EMAIL_VERIFY_AND_SET_PASSWORD.toString());
        t.setTokenHash(hash);
        t.setExpiresAt(expiresAt);
        tokenMapper.insert(t);

        String link = origin + "/auth/set-password?token=" + raw;
        mailSender.send(email,
                "Verify your email & set password",
                "Kia ora,\n\nPlease click the link to verify your email and set your password:\n"
                        + link + "\n\nThis link expires in 24 hours.\n\nNgā mihi,\nTSB" );
    }

    @Override
    @Transactional
    public void setPasswordByToken(String rawToken, String newPassword) {
        String hash = CryptoUtil.sha256(rawToken);

        UserToken t = tokenMapper.selectOne(
                Wrappers.<UserToken>lambdaQuery()
                        .eq(UserToken::getTokenHash, hash)
                        .eq(UserToken::getPurpose, UserTokenPurpose.EMAIL_VERIFY_AND_SET_PASSWORD.toString())
                        .isNull(UserToken::getUsedAt)
                        .gt(UserToken::getExpiresAt, OffsetDateTime.now())
        );

        if (t == null) {
            throw new IllegalArgumentException("invalid_or_expired_token");
        }

        User u = userMapper.selectById(t.getUserId());
        if (u == null) throw new IllegalStateException("user_not_found");


        // 2) Argon2id
        String pwdHash = passwordEncoder.encode(newPassword);

        PasswordCredential cred = passwordCredentialMapper.selectById(u.getId());
        OffsetDateTime now = OffsetDateTime.now();

        cred.setPasswordHash(pwdHash);
        cred.setPasswordAlgo("argon2id");
        cred.setPasswordUpdatedAt(now);
        passwordCredentialMapper.updateById(cred);


        u.setIsEmailVerified(Boolean.TRUE);
        u.setIsPhoneVerified(Boolean.TRUE);
        u.setUpdatedAt(OffsetDateTime.now());
        userMapper.updateById(u);

        t.setUsedAt(OffsetDateTime.now());
        tokenMapper.updateById(t);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest req, String ip, String userAgent) {

        // 1) Check user status
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (u == null) {
            throw BizException.error(ErrorCodes.INVALID_CREDENTIALS.code(), "Invalid username or password");
        };

        // Check user verified
        if (Boolean.FALSE.equals(u.getIsEmailVerified())) {
            throw BizException.error(ErrorCodes.NO_VERIFIED_USER.code(),"Email or phone not verified");
        }

        // 2) Check password
        PasswordCredential cred = passwordCredentialMapper.selectById(u.getId());
        if (cred == null || cred.getPasswordHash() == null) {
            throw BizException.error(ErrorCodes.INVALID_CREDENTIALS.code(), "Invalid username or password");
        }
        if (!passwordEncoder.matches(req.getPassword(), cred.getPasswordHash())) {
            throw BizException.error(ErrorCodes.INVALID_CREDENTIALS.code(), "Invalid username or password");
        }

        // 3) Generate Access & Refresh Token
        String secretStr = authProps.getJwt().getHs256Secret();
        byte[] secret = secretStr.getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) throw new IllegalStateException("JWT HS256 secret must be >= 32 bytes");

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss","tsb-banking");
        claims.put("sub", String.valueOf(u.getId()));
        claims.put("aud","retail");
        claims.put("scope","RETAIL");
        claims.put("channel","CUSTOMER");
        claims.put("amr", List.of("pwd"));
        String access = jwtSigner.sign(claims, authProps.getAccessTtlSecondsPlatform());

        String refreshRaw = TokenUtil.randomUrlToken(32);
        String refreshHash = CryptoUtil.sha256(refreshRaw);

        // 4) 写 sessions（刷新令牌）
        OffsetDateTime nowNz = OffsetDateTime.now(ZoneId.of("Pacific/Auckland"));
        OffsetDateTime exp = nowNz.plusDays(authProps.getSession().getRefreshTtlDays());

        Session s = new Session();
        s.setUserId(u.getId());
        s.setRefreshTokenHash(refreshHash);
        s.setCreatedAt(nowNz);
        s.setExpiresAt(exp);
        s.setScope(UserRoleScope.RETAIL.toString());
        s.setIp(ip);
        s.setUserAgent(userAgent);
        sessionMapper.insert(s);

        return LoginResponse.builder()
                .accessToken(access)
                .refreshToken(refreshRaw)
                .expiresIn((int) authProps.getAccessTtlSecondsPlatform())
                .build();

    }

    @Override
    public StaffLoginResponse staffLogin(String username, String rawPwd, String ip, String ua) {

        // 1) Check user status
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (u == null || !UserStatus.ACTIVE.toString().equals(u.getStatus())) {
            authAudit.record(null, AuthEventType.LOGIN_FAILURE, ip, ua, Map.of("channel","STAFF","reason","USER_NOT_FOUND_OR_INACTIVE"));
            throw BizException.error(ErrorCodes.INVALID_CREDENTIALS.code(), "Invalid username or password");
        }

        // 2) Check password
        PasswordCredential cred = passwordCredentialMapper.selectById(u.getId());
        if (cred == null || cred.getPasswordHash() == null || !passwordEncoder.matches(rawPwd, cred.getPasswordHash())) {
            authAudit.record(u.getId(), AuthEventType.LOGIN_FAILURE, ip, ua, Map.of("channel","STAFF","reason","BAD_PASSWORD"));
            throw BizException.error(ErrorCodes.INVALID_CREDENTIALS.code(), "Invalid username or password");
        }

        // 3) Check staff
        StaffProfile sp = staffProfileMapper.selectById(u.getId());
        if (sp == null || !UserStatus.ACTIVE.toString().equals(sp.getStatus())) {
            authAudit.record(u.getId(), AuthEventType.LOGIN_FAILURE, ip, ua, Map.of("channel","STAFF","reason","NOT_STAFF"));
            throw BizException.error(ErrorCodes.STAFF_NOT_FOUND.code(), "Staff not found");
        }

        // 4) Check staff role
        List<String> roles = platformRoleQueryMapper.selectPlatformRoleCodes(u.getId());
        if (roles == null || roles.isEmpty()) {
            authAudit.record(u.getId(), AuthEventType.LOGIN_FAILURE, ip, ua, Map.of("channel","STAFF","reason","NO_PLATFORM_ROLE"));
            throw BizException.error(ErrorCodes.NO_PLATFORM_ROLE.code(), "No platform permission");
        }
        Set<String> perms = platformRoleQueryMapper.selectPlatformPermissions(u.getId());

        // 5) Generate access token
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss","tsb-banking");
        claims.put("sub", String.valueOf(u.getId()));
        claims.put("aud","platform");
        claims.put("scope","PLATFORM");
        claims.put("channel","STAFF");
        claims.put("roles", roles);
        claims.put("perms", perms);
        claims.put("amr", List.of("pwd"));
        String access = jwtSigner.sign(claims, authProps.getAccessTtlSecondsPlatform());

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        // 6) Generate refresh token and insert in database
        String refresh = TokenUtil.random();
        String refreshHash = TokenUtil.sha256Base64(refresh);
        Session s = new Session();
        s.setUserId(u.getId());
        s.setRefreshTokenHash(refreshHash);
        s.setIp(ip);
        s.setScope("PLATFORM");
        s.setUserAgent(ua);
        s.setCreatedAt(now);
        s.setExpiresAt(now.plusDays(30));
        sessionMapper.insert(s);

        // 7) Record event
        authAudit.record(u.getId(), AuthEventType.LOGIN_SUCCESS, ip, ua, Map.of("channel","STAFF"));

        // 8) Response
        return StaffLoginResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .expiresIn((int) authProps.getAccessTtlSecondsPlatform())
                .tokenType("Bearer")
                .user(Map.of(
                        "id", u.getId(),
                        "username", u.getUsername(),
                        "roles", roles,
                        "perms", perms
                ))
                .build();
    }



    @Transactional
    @Override
    public void sendOtpToPhone(String rawPhoneE164, String ip, String userAgent) {
        String phone = "+64" + rawPhoneE164;

        // Rate limit: deny if a request was created within the last 60 seconds
        OffsetDateTime cut = OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(60);
        Long recent = userTokenMapper.selectCount(new LambdaQueryWrapper<UserToken>()
                .eq(UserToken::getPurpose, UserTokenPurpose.PASSWORD_RESET)
                .gt(UserToken::getCreatedAt, cut)
                .apply("exists (select 1 from users u where u.id = user_tokens.user_id and u.phone_e164 = {0})", phone)
        );
        if (recent != null && recent > 0) {
            // Audit log: MFA_FAILURE (reason: rate_limited)
            recordEvent(null, AuthEventType.MFA_FAILURE, ip, userAgent, JsonUtil.of("reason","rate_limited","phone",mask(phone)));
            throw BizException.error( ErrorCodes.TOO_MANY_REQUESTS.code(), "Too many requests. Try again soon.");
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhoneE164, phone).last("limit 1"));

        // Always return the same response to avoid user enumeration,
        // but still write an audit event
        if (user == null || Boolean.FALSE.equals(user.getIsPhoneVerified())) {
            recordEvent(null, AuthEventType.MFA_CHALLENGE, ip, userAgent, JsonUtil.of("phone",mask(phone),"status","no_user_or_unverified"));
            throw BizException.error(ErrorCodes.USER_NOT_FOUND.code(), "User not found");
        }

        // Generate OTP & store its hash
        String otp = String.format(Locale.ROOT, "%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        String otpHash = passwordEncoder.encode(otp);

        UserToken token = new UserToken();
        token.setUserId(user.getId());
        token.setPurpose(UserTokenPurpose.PASSWORD_RESET.toString());
        token.setTokenHash(otpHash);
        token.setExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5));
        userTokenMapper.insert(token);

        String massage = "[TSB] Your password reset code: " + otp + " (valid 5 min)";

        // Send SMS with OTP
        smsSender.SendOtp(phone, massage);

        // Audit log: MFA_CHALLENGE issued via SMS
        recordEvent(user.getId(), AuthEventType.MFA_CHALLENGE, ip, userAgent, JsonUtil.of("phone",mask(phone),"channel","SMS"));
    }

    @Transactional
    @Override
    public void verifyAndResetPassword(String rawPhoneE164, String otp, String newPassword,
                                       String ip, String userAgent) {
        String phone = "+64" + rawPhoneE164;
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhoneE164, phone).last("limit 1"));

        if (user == null) {
            BizException.error(ErrorCodes.USER_NOT_FOUND.code(), "User not found");
        }

        UserToken token = userTokenMapper.selectOne(new LambdaQueryWrapper<UserToken>()
                .eq(UserToken::getUserId, user.getId())
                .eq(UserToken::getPurpose, UserTokenPurpose.PASSWORD_RESET)
                .isNull(UserToken::getUsedAt)
                .gt(UserToken::getExpiresAt, OffsetDateTime.now(ZoneOffset.UTC))
                .orderByDesc(UserToken::getCreatedAt)
                .last("limit 1"));

        if (token == null || !passwordEncoder.matches(otp, token.getTokenHash())) {
            recordEvent(user.getId(), AuthEventType.MFA_FAILURE, ip, userAgent, JsonUtil.of("reason","bad_or_expired_code","phone",mask(phone)));
            BizException.error(ErrorCodes.VALIDATION_ERROR.code(), "Token doesn't match");
        }

        PasswordCredential current = passwordCredentialMapper.selectById(user.getId());
        if (current != null && current.getPasswordHash() != null) {
            // TODO： PasswordHistoryMapper.insert(userId, current.getPasswordHash())
        }

        // Update with argon2id
        String newHash = passwordEncoder.encode(newPassword);
        PasswordCredential updated = new PasswordCredential();
        updated.setUserId(user.getId());
        updated.setPasswordHash(newHash);
        updated.setPasswordAlgo(extractAlgo(newHash));
        updated.setPasswordUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        passwordCredentialMapper.updateById(updated);

        // Update token revoked
        UserToken used = new UserToken();
        used.setId(token.getId());
        used.setUsedAt(OffsetDateTime.now(ZoneOffset.UTC));
        userTokenMapper.updateById(used);

        // Update user
        user.setIsEmailVerified(true);
        user.setIsPhoneVerified(true);
        userMapper.updateById(user);


        recordEvent(user.getId(), AuthEventType.PASSWORD_RESET, ip, userAgent, JsonUtil.of("by","SMS","phone",mask(phone)));
    }

    @Override
    public void logout(String refreshTokenPlain, String ip, String ua, Boolean fromStaffEndpoint) {
        if (refreshTokenPlain == null || refreshTokenPlain.isBlank()) {
            recordEvent(null, AuthEventType.LOGOUT, ip, ua, JsonUtil.of("reason","MISSING_REFRESH","channel","UNKNOWN"));
            throw  BizException.error( ErrorCodes.NO_REFRESH_TOKEN.code(), "Missing refresh token");
        }

        final String hash = TokenUtil.sha256Base64(refreshTokenPlain);
        Session session = sessionMapper.selectOne(
                new LambdaQueryWrapper<Session>()
                        .eq(Session::getRefreshTokenHash, hash)
        );

        if (session == null) {
            recordEvent(null, AuthEventType.LOGOUT, ip, ua, JsonUtil.of("not_found","***","channel","UNKNOWN"));
            throw BizException.error(ErrorCodes.REFRESH_TOKEN_NOT_ALLOWED.code(), "Refresh token error");
        }

        String channel = UserRoleScope.PLATFORM.name().equalsIgnoreCase(session.getScope()) ? "STAFF" : "RETAIL";
        boolean allowed = (Boolean.TRUE.equals(fromStaffEndpoint) && channel.equals("STAFF"))
                || (!Boolean.TRUE.equals(fromStaffEndpoint) && channel.equals("RETAIL"));

        if (!allowed) {
            recordEvent(session.getUserId(), AuthEventType.LOGOUT, ip, ua,
                    JsonUtil.of("mismatch","platform_does_not_match_with_session","channel",channel));
            throw  BizException.error(ErrorCodes.USER_ROLE_NOT_ALLOWED.code(), "Channel mismatch");
        }

        if (session.getRevokedAt() == null) {
            session.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
            sessionMapper.updateById(session);
        }

        recordEvent(session.getUserId(), AuthEventType.LOGOUT, ip, ua,
                JsonUtil.of("ok", String.valueOf(session.getId()), "channel", channel));

        return;
    }




    // --- helpers ---
    private String extractAlgo(String hash) {
        if (hash != null && hash.startsWith("{") && hash.contains("}")) return hash.substring(1, hash.indexOf('}'));
        return "unknown";
    }
    private String mask(String phone) {
        if (phone == null || phone.length() < 6) return "****";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }
    private void recordEvent(Long userId, AuthEventType type, String ip, String ua, Map<String,Object> meta) {
        AuthEvent e = new AuthEvent();
        e.setUserId(userId);
        e.setEventType(type.toString());
        e.setIpAddress(ip);
        e.setUserAgent(ua);
        e.setMeta(meta);
        authEventMapper.insert(e);
    }

    private boolean usernameTaken(String username) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)) > 0;
    }

}
