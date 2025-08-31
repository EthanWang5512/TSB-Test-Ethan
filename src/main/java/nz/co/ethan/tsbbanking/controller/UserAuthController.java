package nz.co.ethan.tsbbanking.controller;

import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nz.co.ethan.tsbbanking.controller.dto.common.APIResponse;
import nz.co.ethan.tsbbanking.controller.dto.userAuth.request.*;
import nz.co.ethan.tsbbanking.controller.dto.userAuth.response.LoginResponse;
import nz.co.ethan.tsbbanking.controller.dto.userAuth.response.RegisterResponse;
import nz.co.ethan.tsbbanking.controller.dto.userAuth.response.StaffLoginResponse;
import nz.co.ethan.tsbbanking.service.UserAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*; // 包含 org.springframework.web.bind.annotation.RequestBody

import static nz.co.ethan.tsbbanking.util.CookieUtil.getCookie;
import static nz.co.ethan.tsbbanking.util.IpUtil.getClientIp;

// ---- Swagger / OpenAPI (注意：不要导入 Swagger 的 RequestBody 类以免冲突) ----
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User Authentication",
        description = "Authentication & account lifecycle APIs for staff and retail users; Set access token as bearerAuth, set refresh token in X-Refresh-Token")
public class UserAuthController {

    private final UserAuthService userAuthService;

    // ----------------------------------------------------------------------
    // PUBLIC (no token)
    // ----------------------------------------------------------------------

    @PostMapping("/staff/login")
    @Operation(
            summary = "Staff sign-in",
            description = "Authenticate a staff user with username/password. Returns platform-scoped access/refresh tokens.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Staff login credentials",
                    required = true,
                    content = @Content(schema = @Schema(implementation = StaffLoginRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Default",
                                            value = "{ \"username\": \"admin\", \"password\": \"123456\" }"
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Signed in",
                            content = @Content(schema = @Schema(implementation = StaffLoginResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    public StaffLoginResponse staffLogin(
            @org.springframework.web.bind.annotation.RequestBody StaffLoginRequest req,
            @Parameter(description = "Forwarded client IP (first value used)", example = "203.0.113.10", hidden = true)
            @RequestHeader(value="X-Forwarded-For", required=false) String xff,
            @Parameter(description = "Caller user agent", example = "Mozilla/5.0 ...", hidden = true)
            @RequestHeader(value="User-Agent", required=false) String ua,
            HttpServletRequest http) {
        String ip = (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : http.getRemoteAddr();
        return userAuthService.staffLogin(req.getUsername(), req.getPassword(), ip, ua);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Retail sign-in",
            description = "Authenticate a retail user with username/email/phone and password. Returns retail-scoped access/refresh tokens.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Retail login request",
                    content = @Content(schema = @Schema(implementation = LoginRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Default",
                                            value = "{ \"username\": \"10000001\", \"password\": \"123456\" }"
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Signed in",
                            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    public ResponseEntity<LoginResponse> login(
            @org.springframework.web.bind.annotation.RequestBody @Valid LoginRequest req,
            HttpServletRequest http) {
        String ip = getClientIp(http);
        String ua = http.getHeader("User-Agent");
        return ResponseEntity.ok(userAuthService.login(req, ip, ua));
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Set password by token (public no token)",
            description = "Set/reset password using a one-time token from email.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Token and new password",
                    content = @Content(schema = @Schema(implementation = SetPasswordByTokenRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password set"),
                    @ApiResponse(responseCode = "400", description = "Invalid/expired token")
            }
    )
    public ResponseEntity<APIResponse<Void>> setPassword(
            @org.springframework.web.bind.annotation.RequestBody @Valid SetPasswordByTokenRequest req) {
        userAuthService.setPasswordByToken(req.getToken(), req.getNewPassword());
        return ResponseEntity.ok(APIResponse.success("Password set successfully"));
    }

    @PostMapping("/request-reset-password-otp")
    @Operation(
            summary = "Request password reset OTP (public no token)",
            description = "Send an OTP to the provided E.164 phone number for password reset.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Phone number in E.164 format",
                    content = @Content(schema = @Schema(implementation = SendResetPasswordOtpRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OTP sent",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid phone")
            }
    )
    public ResponseEntity<APIResponse<Void>> requestOtp(
            @org.springframework.web.bind.annotation.RequestBody @Valid SendResetPasswordOtpRequest req,
            HttpServletRequest http) {
        String ip = getClientIp(http);
        String ua = http.getHeader("User-Agent");
        userAuthService.sendOtpToPhone(req.getPhoneE164(), ip, ua);
        return ResponseEntity.ok(APIResponse.success("OTP send successfully"));
    }

    @PostMapping("/reset-password-otp")
    @Operation(
            summary = "Reset password by OTP (public no token)",
            description = "Verify the OTP and set a new password.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Phone, OTP and new password",
                    content = @Content(schema = @Schema(implementation = ResetPasswordByOtpRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password reset",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid phone/OTP"),
                    @ApiResponse(responseCode = "410", description = "OTP expired")
            }
    )
    public ResponseEntity<APIResponse<Void>> verify(
            @org.springframework.web.bind.annotation.RequestBody @Valid ResetPasswordByOtpRequest req,
            HttpServletRequest http) {
        String ip = getClientIp(http);
        String ua = http.getHeader("User-Agent");
        userAuthService.verifyAndResetPassword(req.getPhoneE164(), req.getOtp(), req.getNewPassword(), ip, ua);
        return ResponseEntity.ok(APIResponse.success("Password reset successfully"));
    }

    // ----------------------------------------------------------------------
    // SECURED (need BOTH tokens: Authorization: Bearer + X-Refresh-Token)
    // ----------------------------------------------------------------------

    @PostMapping("/staff/logout")
    @Operation(
            summary = "Staff sign-out (with staff login token)",
            description = "Invalidate the current refresh token and revoke staff session.",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "refreshAuth")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Logged out",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Missing/invalid refresh token"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<APIResponse<Void>> staffLogout(
            @Parameter(description = "Refresh token in header; if absent, server will try cookie 'refresh_token'", hidden = true)
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken,
            HttpServletRequest req) {
        if (refreshToken == null || refreshToken.isBlank()) {
            refreshToken = getCookie(req, "refresh_token");
        }
        userAuthService.logout(refreshToken, getClientIp(req), req.getHeader("User-Agent"), true);
        return ResponseEntity.ok(APIResponse.success("Logout successfully"));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Retail sign-out with retail login token",
            description = "Invalidate the current refresh token and revoke retail session.",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "refreshAuth")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Logged out",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Missing/invalid refresh token"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<APIResponse<Void>> retailLogout(
            @Parameter(description = "Refresh token in header; if absent, server will try cookie 'refresh_token'", hidden = true)
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken,
            HttpServletRequest req) {
        if (refreshToken == null || refreshToken.isBlank()) {
            refreshToken = getCookie(req, "refresh_token");
        }
        userAuthService.logout(refreshToken, getClientIp(req), req.getHeader("User-Agent"), false);
        return ResponseEntity.ok(APIResponse.success("Logout successfully"));
    }

    @PostMapping("/register")
    @Operation(
            summary = "Create a new user (with staff login token)",
            description = "Provision a new user using email and phone.",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "refreshAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Email and phone to register",
                    content = @Content(schema = @Schema(implementation = RegisterRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User created or existed",
                            content = @Content(schema = @Schema(implementation = RegisterResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<RegisterResponse> register(
            @Validated @org.springframework.web.bind.annotation.RequestBody RegisterRequest req) {
        var user = userAuthService.registerUser(req.getEmail(), req.getPhone());
        return ResponseEntity.ok(new RegisterResponse(user.getUsername()));
    }

    @PostMapping("/send-verify-email")
    @Operation(
            summary = "Send verification email & set-password link (with staff login token)",
            description = "Send a verification email with a one-time link for setting initial password.",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "refreshAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Recipient email and username",
                    content = @Content(schema = @Schema(implementation = SendVerifyEmailRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email dispatched",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<APIResponse<Void>> sendVerifyEmailAndSetPassword(
            @org.springframework.web.bind.annotation.RequestBody @Valid SendVerifyEmailRequest req,
            @Parameter(hidden = true)
            @RequestHeader(value="Origin", required=false) String origin) {
        String base = (origin != null && !origin.isBlank()) ? origin : "http://localhost:5173";
        userAuthService.sendVerifyEmailAndSetPasswordLink(req.getEmail(), req.getUsername(), base);
        return ResponseEntity.ok(APIResponse.success("Email send successfully"));
    }
}
